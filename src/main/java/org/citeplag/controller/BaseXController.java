package org.citeplag.controller;

import org.citeplag.basex.BaseXClient;
import org.citeplag.basex.Client;
import org.citeplag.basex.Server;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.citeplag.config.BaseXConfig;
import org.citeplag.domain.MathRequest;
import org.citeplag.domain.MathUpdate;
import org.citeplag.beans.BaseXGenericResponse;
import org.citeplag.util.ChecksumCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

/**
 * Imported from a RestX frontend. Originally from mathosphere/restd.
 *
 * @author Andre Greiner-Petter
 */
@RestController
@RequestMapping("/basex")
public class BaseXController {
    private static final Logger LOG = LogManager.getLogger(BaseXController.class.getName());

    // the final BaseX server
    private static final Server BASEX_SERVER = Server.getInstance();
    private static boolean serverRunning = false;
    // Creating checksums for watched folder of harvests.
    private final ChecksumCreator checksumCreator = new ChecksumCreator();
    private Map<String, String> previousChecksums = null;
    @Value("${server.enable_rest_insertions}")
    private boolean enableRestInsertions;

    @Autowired
    private BaseXConfig baseXConfig;

    @PostMapping
    @ApiOperation(value = "Run query on BaseX")
    public MathRequest processing(
            @RequestParam @ApiParam(allowableValues = "tex, xquery, mws") String type,
            @RequestParam String query,
            HttpServletRequest request) {
        return process(query, type, request);
    }

    @PostMapping("/texquery")
    @ApiOperation(value = "Run TeX query on BaseX")
    public MathRequest texProcessing(
            @RequestParam String query,
            HttpServletRequest request) {
        return process(query, "tex", request);
    }

    @PostMapping("/xquery")
    @ApiOperation(value = "Run XQuery on BaseX")
    public MathRequest xQueryProcessing(
            @RequestParam String query,
            HttpServletRequest request) {
        return process(query, "xquery", request);
    }

    @PostMapping("/mwsquery")
    @ApiOperation(value = "Run MWS query on BaseX")
    public MathRequest mwsProcessing(@RequestBody String data, HttpServletRequest request) {
        JSONObject jsonObject = extractJSONFromData(data);
        if (jsonObject == null) {
            return null;
        }
        String query = processJSONquery(jsonObject);
        if (query == null) {
            return null;
        }

        return process(query, "mws", request);
    }

    /**
     * Drafted endpoint  for export functionality.
     * It is possible that this will be not an endpoint in final mardi-portal, but here for dev-purposes.
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/export")
    @ApiOperation(value = "Export BaseX database to file. Relative and absoulte filepath to export file possible.")
    public BaseXGenericResponse exportFromBaseX(@RequestBody String data, HttpServletRequest request) {
        if (!enableRestInsertions) {
            // This is a security setting for deployment in prod.
            return null;
        }

        JSONObject jsonObject = extractJSONFromData(data);
        if (jsonObject == null) {
            return new BaseXGenericResponse(1, "No input data defined");
        }
        String path = "";
        try {
            path = jsonObject.get("path").toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return new BaseXGenericResponse(1, "Problem exporting data: " + e.getMessage());
        }

        // Starting Base-X.
        if (!startServerIfNecessary()) {
            LOG.warn("Return null for request, because BaseX server is not running.");
            return null;
        }
        return doExport(path);
    }


    public BaseXGenericResponse doExport(String filepath) {
        try {
            BaseXClient baseXClient =  Client.getBaseXClient();
            baseXClient.execute("EXPORT " + filepath);
            return new BaseXGenericResponse(0, "Successfully Exported data to: " + filepath);
        } catch (IOException e) {
            return new BaseXGenericResponse(1, "Problem exporting data: " + e.getMessage());
        }
    }


    /**
     * Processing a json-formatted data object for query.
     * @param jsonObject object for data which contains query string.
     * @return parsed query as string.
     */
    private String processJSONquery(JSONObject jsonObject) {
        String query;
        try {
            query = jsonObject.get("query").toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return query;
    }

    /**
     * Extracting the query input from url encoded string of data.
     * This is required to handle requests coming from FormulaSearch-extension.
     * @param data url encoded string which container query as json.
     * @return query as string or data
     */
    private JSONObject extractJSONFromData(String data) {
        JSONObject resultJ = null;
        try {
            String result = java.net.URLDecoder.decode(data, StandardCharsets.UTF_8.name());
            resultJ = new JSONObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJ;
    }

    private MathRequest process(String query, String type, HttpServletRequest request) {
        if (!startServerIfFolderChanges()) {
            LOG.warn("Return null for request, because BaseX server is not running.");
            return null;
        }
        LOG.info("BaseX processing request from: " + request.getRemoteAddr());
        MathRequest mreq = new MathRequest(query);
        mreq.setType(type);
        MathRequest result = mreq.run();
        return result;
    }

    @PostMapping("/update")
    @ApiOperation(value = "Update results via BaseX")
    public MathUpdate update(@RequestBody String data, HttpServletRequest request) {
        if (!enableRestInsertions) {
            // This is a security setting for deployment in prod.
            return null;
        }

        JSONObject jsonObject = extractJSONFromData(data);
        if (jsonObject == null) {
            return null;
        }
        // Parsing the input-paramters.
        String harvest;
        String secureHarvest;
        Integer[] delete;
        try {
            harvest = jsonObject.get("harvest").toString();
            // replace null by empty string to avoid null pointers.
            secureHarvest = harvest == null ? "" : harvest;
            delete = parseArray(jsonObject.get("delete").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // Starting Base-X.
        if (!startServerIfNecessary()) {
            LOG.warn("Return null for request, because BaseX server is not running.");
            return null;
        }
        LOG.info("Request updating given math from: " + request.getRemoteAddr());

        // Updating the harvest.
        MathUpdate mu = new MathUpdate(delete, secureHarvest);
        MathUpdate res = mu.run();

        // Refresh the stored index files after update (necessary with MAINMEM true basex setting)
        doExport(baseXConfig.getHarvestPath());
        return res;
    }
     /**
     * Try to parse an integer array given as a string.
     * If the given string is null or empty, or the string cannot be parsed
     * it will return an empty array.
     *
     * @param integerArray array which is parsed
     * @return integer array with content
     */
    private Integer[] parseArray(String integerArray) throws NumberFormatException {
        if (integerArray == null || integerArray.isEmpty()) {
            return new Integer[0];
        }

        String cleaned = integerArray.replaceAll("\\[|\\]|\\s", "");
        String[] elements = cleaned.split(",");
        Integer[] delete = new Integer[elements.length];
        for (int i = 0; i < elements.length; i++) {
            delete[i] = Integer.parseInt(elements[i]);
        }

        LOG.info("Parsed integer arguments: " + Arrays.toString(delete));
        return delete;
    }

    @PostMapping("/countRev")
    @ApiOperation(value = "Count the number of formulae with specified revision number")
    public Integer dvsize(
            @RequestParam Integer revision,
            HttpServletRequest request) {
        if (!startServerIfNecessary()) {
            LOG.warn("Return null for request, because BaseX server is not running.");
            return null;
        }
        LOG.info("BaseX request to count number of formulae with revision number from: " + request.getRemoteAddr());
        Client client = new Client();
        return client.countRevisionFormula(revision);
    }

    @PostMapping("/countAll")
    @ApiOperation(value = "Count the total number of formulae")
    public Integer dvsize(HttpServletRequest request) {
        if (!startServerIfNecessary()) {
            LOG.warn("Return null for request, because BaseX server is not running.");
            return null;
        }
        LOG.info("BaseX request to count total number of formulae from: " + request.getRemoteAddr());
        Client client = new Client();
        return client.countAllFormula();
    }

    @PostMapping("/restartBaseXServer")
    @ApiOperation(value = "Restarts the BaseX server with another harvest file.")
    public BaseXGenericResponse restart(
            @RequestParam("Path") @ApiParam(
                    name = "Path",
                    value = "Path to harvest file (linux line separators)!",
                    required = true)
                    String harvestPath,
            HttpServletRequest request) {
        if (!enableRestInsertions) {
            // This is a security setting for deployment in prod.
            return null;
        }
        if (harvestPath == null || harvestPath.isEmpty()) {
            return new BaseXGenericResponse(1, "Empty path! Didn't restart the server.");
        }

        String oldHarvest = baseXConfig.getHarvestPath();

        try {
            Path path = Paths.get(harvestPath);

            if (!Files.exists(path)) {
                return new BaseXGenericResponse(1, "Given file does not exist! Didn't restart server.");
            }

            baseXConfig.setHarvestPath(harvestPath);

            BASEX_SERVER.startup(path.toFile());
            return new BaseXGenericResponse(0, "Restarted BaseX server with harvest file " + path.toString());
        } catch (InvalidPathException ipe) {
            return new BaseXGenericResponse(1, "Cannot parse given string to a path, "
                    + "didn't restart server! Reason: " + ipe.getReason());
        } catch (IOException ioe) {
            // reset settings
            serverRunning = false;
            baseXConfig.setHarvestPath(oldHarvest);
            return new BaseXGenericResponse(1, "Cannot restart the BaseX server. " + ioe.toString());
        }
    }

    /**
     * (Re-)starts the server if the folder contents of harvest files have changed.
     * This creates a list of checksums for files in harvests dir and compares it to a previously created list.
     * If the lists differ, the Basex-server is restarted so that the file changes are recognized.
     * @return boolean if server running
     */
    private boolean startServerIfFolderChanges() {
        try {
            Map<String, String> newChecksums = checksumCreator.createChecksumsForFolder(baseXConfig.getHarvestPath(),
                    "SHA-256");
            if (!checksumCreator.compareChecksums(newChecksums, previousChecksums)) {
                startServer();
            }
            previousChecksums = newChecksums;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Wrong algorithm specified for harvest.", e);
        } catch (IOException e) {
            LOG.error("Cannot load harvest file to start BaseX server.", e);
        }
        return serverRunning;
    }
    private boolean startServerIfNecessary() {
        if (!serverRunning) {
            startServer();
        }
        return serverRunning;
    }

    private void startServer() {
        LOG.info("Startup basex server with harvest file: " + baseXConfig.getHarvestPath());
        Path path = Paths.get(baseXConfig.getHarvestPath());
        try {
            BASEX_SERVER.startup(path.toFile());
            serverRunning = true;
        } catch (IOException e) {
            LOG.error("Cannot load harvest file to start BaseX server.", e);
        }
    }
}
