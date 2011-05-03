package mapfishapp.ws;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Main controller that display the extractor home page
 * 
 * @author bruno.binet@camptocamp.com
 */

@Controller
@RequestMapping("/edit")
public class EditController extends AbstractController {
    /**
     * POST entry point.
     * 
     * @param request
     *            . Must contains information from the layers and services to be
     *            extracted
     * @param response
     *            .
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String str = getPostData(request);

        Map<String, Object> model = createModelFromStringOrSession(request, str);

        model.put("edit", isEditor(request));
        

        return new ModelAndView("index", "c", model);
    }

    private Object isEditor(HttpServletRequest request) {
        String sec_roles = request.getHeader("sec-roles");
        if(sec_roles != null) {
            String[] roles = sec_roles.split(",");
            for (int i = 0; i < roles.length; i++) {
                if(roles[i].equals("ROLE_ANONYMOUS")) {
                    // default is anonymous already
                    break;
                }
                else if (roles[i].equals("ROLE_SV_EDITOR") || roles[i].equals("ROLE_SV_REVIEWER") || roles[i].equals("ROLE_SV_ADMIN")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * GET entry point.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        String str = request.getParameter("data");

        Map<String, Object> model = createModelFromStringOrSession(request, str);
        model.put("edit", isEditor(request));

        return new ModelAndView("index", "c", model);
    }

}
