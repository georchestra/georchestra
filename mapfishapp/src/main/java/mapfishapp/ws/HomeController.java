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
@RequestMapping("/home")
public class HomeController extends AbstractController {
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

        return new ModelAndView("index", "c", model);
    }

    /**
     * GET entry point.
     * 
     * @param request
     *            .
     * @param response
     *            .
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleGETRequest(HttpServletRequest request, HttpServletResponse response) {
        String str = request.getParameter("data");

        Map<String, Object> model = createModelFromStringOrSession(request, str);

        return new ModelAndView("index", "c", model);
    }

}
