package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for accessing/modifying user metadata
 * @author Josh Vote
 */
@Controller
public class UserController extends BasePortalController {
    private static final String CLOUD_FORMATION_RESOURCE = "org/auscope/portal/server/web/controllers/vl-cloudformation.json.tpl";

    protected final Log logger = LogFactory.getLog(getClass());

    private ANVGLUserDao userDao;
    private PortalPropertyPlaceholderConfigurer properties;
    private VelocityEngine velocityEngine;

    @Autowired
    public UserController(ANVGLUserDao userDao, PortalPropertyPlaceholderConfigurer properties, VelocityEngine velocityEngine) {
        super();
        this.userDao = userDao;
        this.properties = properties;
        this.velocityEngine = velocityEngine;
    }

    /**
     * Gets user metadata for the currently logged in user
     * @param user
     * @return
     */
    @RequestMapping("/secure/getUser.do")
    public ModelAndView getUser(@AuthenticationPrincipal ANVGLUser user) {
        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        ModelMap userObj = new ModelMap();
        userObj.put("id", user.getId());
        userObj.put("email", user.getEmail());
        userObj.put("fullName", user.getFullName());
        userObj.put("arnExecution", user.getArnExecution());
        userObj.put("arnStorage", user.getArnStorage());
        userObj.put("acceptedTermsConditions", user.getAcceptedTermsConditions());

        return generateJSONResponseMAV(true, userObj, "");
    }

    /**
     * Sets a variety of parameters for the currently logged in user
     * @param user
     * @param arnExecution
     * @param arnStorage
     * @param acceptedTermsConditions
     * @return
     */
    @RequestMapping("/secure/setUser.do")
    public ModelAndView setUser(@AuthenticationPrincipal ANVGLUser user,
            @RequestParam(required=false, value="arnExecution") String arnExecution,
            @RequestParam(required=false, value="arnStorage") String arnStorage,
            @RequestParam(required=false, value="awsAccount") String awsAccount,
            @RequestParam(required=false, value="acceptedTermsConditions") Integer acceptedTermsConditions) {

        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        boolean modified = false;
        if (!StringUtils.isEmpty(arnExecution)) {
            user.setArnExecution(arnExecution);
            modified = true;
        }

        if (!StringUtils.isEmpty(arnStorage)) {
            user.setArnStorage(arnStorage);
            modified = true;
        }

        if (acceptedTermsConditions != null) {
            user.setAcceptedTermsConditions(acceptedTermsConditions);
            modified = true;
        }

        if (awsAccount != null) {
            user.setAwsAccount(awsAccount);
            modified = true;
        }

        if (modified) {
            userDao.save(user);
        }

        return generateJSONResponseMAV(true);
    }

    @RequestMapping("/getTermsConditions.do")
    public ModelAndView getTermsConditions(@AuthenticationPrincipal ANVGLUser user) {

        try {
            String tcs = IOUtils.toString(this.getClass().getResourceAsStream("vl-termsconditions.html"));

            ModelMap response = new ModelMap();
            response.put("html", tcs);
            response.put("currentVersion", Integer.parseInt(properties.resolvePlaceholder("termsconditions.version")));
            if (user != null) {
                response.put("acceptedVersion", user.getAcceptedTermsConditions());
            }

            return generateJSONResponseMAV(true, response, "");
        } catch (IOException ex) {
            log.error("Unable to read terms and conditions resource", ex);
            return generateJSONResponseMAV(false);
        }
    }

    @RequestMapping("/secure/getCloudFormationScript.do")
    public ModelAndView getCloudFormationScript(@AuthenticationPrincipal ANVGLUser user) {
        if (user == null || !user.isFullyConfigured()) {
            return generateJSONResponseMAV(false);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("arnStorage", user.getArnStorage());
        model.put("arnExecute", user.getArnExecution());
        model.put("awsSecret", user.getAwsSecret());
        model.put("awsAccount", user.getAwsAccount());

        String cloudFormationScript = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, CLOUD_FORMATION_RESOURCE, "UTF-8", model);
        return generateJSONResponseMAV(true, cloudFormationScript, "");
    }
}