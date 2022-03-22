package org.citeplag.controller;

import com.formulasearchengine.mathmltools.converters.config.LaTeXMLConfig;
import io.swagger.annotations.ApiOperation;
import org.citeplag.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * REST Controller to load the current configuration.s
 *
 * @author Vincent Stange
 */
@RestController
@RequestMapping("config")
public class ConfigController {

    @Autowired
    private LaTeXMLRemoteConfig laTeXMLRemoteConfig;


    @Autowired
    private BaseXConfig baseXConfig;

    @GetMapping("latexml")
    @ApiOperation(value = "Show the current default LaTeXML configuration")
    public LaTeXMLConfig getLaTeXMLConfig(HttpServletRequest request) throws Exception {
        return laTeXMLRemoteConfig;
    }
    @GetMapping("basex")
    @ApiOperation(value = "Show the BaseX server configuration")
    public BaseXConfig getBaseXConfig(HttpServletRequest request) {
        return baseXConfig;
    }
}