/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: Apache Tomcat/9.0.62
 * Generated at: 2023-09-20 06:23:38 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class tokenGen_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent,
                 org.apache.jasper.runtime.JspSourceImports {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private static final java.util.Set<java.lang.String> _jspx_imports_packages;

  private static final java.util.Set<java.lang.String> _jspx_imports_classes;

  static {
    _jspx_imports_packages = new java.util.HashSet<>();
    _jspx_imports_packages.add("javax.servlet");
    _jspx_imports_packages.add("javax.servlet.http");
    _jspx_imports_packages.add("javax.servlet.jsp");
    _jspx_imports_classes = null;
  }

  private volatile javax.el.ExpressionFactory _el_expressionfactory;
  private volatile org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public java.util.Set<java.lang.String> getPackageImports() {
    return _jspx_imports_packages;
  }

  public java.util.Set<java.lang.String> getClassImports() {
    return _jspx_imports_classes;
  }

  public javax.el.ExpressionFactory _jsp_getExpressionFactory() {
    if (_el_expressionfactory == null) {
      synchronized (this) {
        if (_el_expressionfactory == null) {
          _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
        }
      }
    }
    return _el_expressionfactory;
  }

  public org.apache.tomcat.InstanceManager _jsp_getInstanceManager() {
    if (_jsp_instancemanager == null) {
      synchronized (this) {
        if (_jsp_instancemanager == null) {
          _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
        }
      }
    }
    return _jsp_instancemanager;
  }

  public void _jspInit() {
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
      throws java.io.IOException, javax.servlet.ServletException {

    if (!javax.servlet.DispatcherType.ERROR.equals(request.getDispatcherType())) {
      final java.lang.String _jspx_method = request.getMethod();
      if ("OPTIONS".equals(_jspx_method)) {
        response.setHeader("Allow","GET, HEAD, POST, OPTIONS");
        return;
      }
      if (!"GET".equals(_jspx_method) && !"POST".equals(_jspx_method) && !"HEAD".equals(_jspx_method)) {
        response.setHeader("Allow","GET, HEAD, POST, OPTIONS");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "JSPs only permit GET, POST or HEAD. Jasper also permits OPTIONS");
        return;
      }
    }

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("<meta  name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
      out.write("</head>\n");
      out.write("    <style>\n");
      out.write("        button {\n");
      out.write("            background-color: #4caf50; /* Green */\n");
      out.write("            border: none;\n");
      out.write("            color: white;\n");
      out.write("            padding: 5px 12px;\n");
      out.write("            text-align: center;\n");
      out.write("            text-decoration: none;\n");
      out.write("            display: inline-block;\n");
      out.write("            font-size: 16px;\n");
      out.write("            margin: 4px 2px;\n");
      out.write("            cursor: pointer;\n");
      out.write("            -webkit-transition-duration: 0.4s; /* Safari */\n");
      out.write("            transition-duration: 0.4s;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        button:hover {\n");
      out.write("            box-shadow: 0 12px 16px 0 rgba(0, 0, 0, 0.24), 0 17px 50px 0 rgba(0, 0, 0, 0.19);\n");
      out.write("        }\n");
      out.write("<body>\n");
      out.write("</style>\n");
      out.write("<script>\n");
      out.write("async function copyToClipboard(textToCopy) {\n");
      out.write("    if (false && navigator.clipboard && window.isSecureContext) {\n");
      out.write("        await navigator.clipboard.writeText(textToCopy);\n");
      out.write("    } else {\n");
      out.write("\n");
      out.write("        const textArea = document.createElement(\"textarea\");\n");
      out.write("        textArea.value = textToCopy;\n");
      out.write("\n");
      out.write("        textArea.style.position = \"absolute\";\n");
      out.write("        textArea.style.left = \"-999999px\";\n");
      out.write("\n");
      out.write("        document.body.prepend(textArea);\n");
      out.write("        textArea.select();\n");
      out.write("\n");
      out.write("        try {\n");
      out.write("            document.execCommand('copy');\n");
      out.write("        } catch (error) {\n");
      out.write("            console.error(error);\n");
      out.write("        } finally {\n");
      out.write("            textArea.remove();\n");
      out.write("        }\n");
      out.write("    }\n");
      out.write("}\n");
      out.write("function getDomain()\n");
      out.write("{\n");
      out.write("let dc = document.querySelector('input[name=\"dc\"]:checked').value;\n");
      out.write("if(dc == \"dev\")\n");
      out.write("return \"https://accounts.csez.zohocorpin.com/oauth/v2\";\n");
      out.write("if(dc == \"local\")\n");
      out.write("return \"https://accounts.localzoho.com/oauth/v2\";\n");
      out.write("if(dc == \"us\")\n");
      out.write("return \"https://accounts.zoho.com/oauth/v2\";\n");
      out.write("if(dc == \"in\")\n");
      out.write("return \"https://accounts.zoho.in/oauth/v2\";\n");
      out.write("}\n");
      out.write("function redirect()\n");
      out.write("{\n");
      out.write("    if(document.querySelector('input[name=\"dc\"]:checked') == null)\n");
      out.write("    {\n");
      out.write("        alert(\"Please choose DC\");\n");
      out.write("        return;\n");
      out.write("    }\n");
      out.write("    if(document.getElementById(\"client_id\").value == \"\")\n");
      out.write("    {\n");
      out.write("      alert(\"Client ID is mandatory\");\n");
      out.write("      return;\n");
      out.write("     }\n");
      out.write("     if(document.getElementById(\"scope\").value == \"\")\n");
      out.write("     {\n");
      out.write("         alert(\"Scope is mandatory\");\n");
      out.write("         return;\n");
      out.write("      }\n");
      out.write("      if(document.getElementById(\"redirect_uri\").value == \"\")\n");
      out.write("      {\n");
      out.write("           alert(\"Redirect URI is mandatory\");\n");
      out.write("           return;\n");
      out.write("      }\n");
      out.write("    window.open (getDomain() + \"/auth\" + \"?scope=\" + document.getElementById(\"scope\").value + \"&client_id=\" + document.getElementById(\"client_id\").value + \"&response_type=code&access_type=offline&redirect_uri=\" + document.getElementById(\"redirect_uri\").value + \"&prompt=consent\");\n");
      out.write("}\n");
      out.write("function getTokens()\n");
      out.write("{\n");
      out.write("    if(document.querySelector('input[name=\"dc\"]:checked') == null)\n");
      out.write("    {\n");
      out.write("        alert(\"Please choose DC\");\n");
      out.write("        return;\n");
      out.write("    }\n");
      out.write("if(document.getElementById(\"client_id\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Client ID is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("if(document.getElementById(\"client_secret\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Client Secret is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("if(document.getElementById(\"redirect_uri\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Redirect URI is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("if(document.getElementById(\"redirected_uri\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Redirected URI is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("document.getElementById(\"response\").style.display=\"block\";\n");
      out.write("document.getElementById(\"output\").value = \"Generating ........\";\n");
      out.write("\n");
      out.write("const params = new URLSearchParams();\n");
      out.write("params.append('code', document.getElementById(\"redirected_uri\").value.match(/code=([^&]+)/)[1]);\n");
      out.write("params.append('client_id', document.getElementById(\"client_id\").value);\n");
      out.write("params.append('client_secret', document.getElementById(\"client_secret\").value);\n");
      out.write("params.append('redirect_uri', document.getElementById(\"redirect_uri\").value);\n");
      out.write("params.append('grant_type', \"authorization_code\");\n");
      out.write("params.append('url', getDomain() + \"/token\");\n");
      out.write("\n");
      out.write("fetch( \"/api/v1/oauth/tokens?\" + params, {\n");
      out.write("     method: \"POST\",\n");
      out.write("      headers: {\n");
      out.write("          'Content-Type' : 'application/x-www-form-urlencoded'\n");
      out.write("      },\n");
      out.write("})\n");
      out.write(".then(response =>\n");
      out.write("{\n");
      out.write("return response.text();\n");
      out.write("}\n");
      out.write(").then(data=> {\n");
      out.write("document.getElementById(\"output\").value = JSON.stringify(JSON.parse(data), null, 2);\n");
      out.write("}).catch(error => {\n");
      out.write("   document.getElementById(\"output\").value = \"Something went wrong. Try again\";\n");
      out.write("});\n");
      out.write("}\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("function refresh()\n");
      out.write("{\n");
      out.write("if(document.querySelector('input[name=\"dc\"]:checked') == null)\n");
      out.write("    {\n");
      out.write("        alert(\"Please choose DC\");\n");
      out.write("        return;\n");
      out.write("}\n");
      out.write("if(document.getElementById(\"client_id\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Client ID is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("if(document.getElementById(\"client_secret\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Client Secret is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("if(document.getElementById(\"refresh_token\").value == \"\")\n");
      out.write("{\n");
      out.write("   alert(\"Refresh token is mandatory to get token\");\n");
      out.write("   return;\n");
      out.write("}\n");
      out.write("document.getElementById(\"response\").style.display=\"block\";\n");
      out.write("document.getElementById(\"output\").value = \"Generating ........\";\n");
      out.write("\n");
      out.write("\n");
      out.write("const params = new URLSearchParams();\n");
      out.write("params.append('client_id', document.getElementById(\"client_id\").value);\n");
      out.write("params.append('client_secret', document.getElementById(\"client_secret\").value);\n");
      out.write("params.append('refresh_token', document.getElementById(\"refresh_token\").value);\n");
      out.write("params.append('grant_type', \"refresh_token\");\n");
      out.write("params.append('url', getDomain() + \"/token\");\n");
      out.write("fetch( \"/api/v1/oauth/tokens?\" + params, {\n");
      out.write("     method: \"POST\",\n");
      out.write("      headers: {\n");
      out.write("          'Content-Type' : 'application/x-www-form-urlencoded'\n");
      out.write("      },\n");
      out.write("})\n");
      out.write(".then(response =>\n");
      out.write("{\n");
      out.write("return response.text();\n");
      out.write("}\n");
      out.write(").then(data=> {\n");
      out.write("document.getElementById(\"output\").value = JSON.stringify(JSON.parse(data), null, 2);\n");
      out.write("}).catch(error => {\n");
      out.write("  document.getElementById(\"output\").value = \"Something went wrong. Try again\";\n");
      out.write("});\n");
      out.write("}\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("function copyAT(){\n");
      out.write("try{\n");
      out.write("const json = JSON.parse(document.getElementById(\"output\").value);\n");
      out.write("if(json.access_token!=undefined){\n");
      out.write("copyToClipboard(json.access_token);\n");
      out.write("alert(\"copied\");}\n");
      out.write("else\n");
      out.write("alert(\"Access token not found in response\");\n");
      out.write("}\n");
      out.write("catch(err)\n");
      out.write("{\n");
      out.write("alert(\"Access token not found in response\");\n");
      out.write("}\n");
      out.write("}\n");
      out.write("function copyRT(){\n");
      out.write("try{\n");
      out.write("const json = JSON.parse(document.getElementById(\"output\").value);\n");
      out.write("if(json.refresh_token!=undefined){\n");
      out.write("copyToClipboard(json.refresh_token);\n");
      out.write("alert(\"copied\");}\n");
      out.write("else\n");
      out.write("alert(\"Refresh token not found in response\");\n");
      out.write("}\n");
      out.write("catch(err)\n");
      out.write("{\n");
      out.write("alert(\"Refresh token not found in response\");\n");
      out.write("}\n");
      out.write("}\n");
      out.write("function copyAll(){\n");
      out.write("copyToClipboard(document.getElementById(\"output\").value);\n");
      out.write("alert(\"copied\");\n");
      out.write("}\n");
      out.write("\n");
      out.write("function populate()\n");
      out.write("{\n");
      out.write("    reset(true);\n");
      out.write("    if(document.querySelector('input[name=\"dc\"]:checked') == null)\n");
      out.write("    {\n");
      out.write("        alert(\"Please choose DC\");\n");
      out.write("        return;\n");
      out.write("    }\n");
      out.write("    var password = \"\";\n");
      out.write("    if(document.querySelector('input[name=\"dc\"]:checked').value!=\"dev\" && document.querySelector('input[name=\"dc\"]:checked').value !=\"local\"){\n");
      out.write("    password = prompt(\"Enter password to populate credentials or click cancel and use your own credentials.\");\n");
      out.write("    if(password.length == 0)\n");
      out.write("    {\n");
      out.write("    return;\n");
      out.write("    }\n");
      out.write("    }\n");
      out.write("    else\n");
      out.write("     document.getElementById(\"populate\").style.display=\"none\";\n");
      out.write("    fetch( \"/api/v1/populateSecrets?password=\" + password + \"&dc=\" + document.querySelector('input[name=\"dc\"]:checked').value, {\n");
      out.write("         method: \"GET\",\n");
      out.write("          headers: {\n");
      out.write("              'Content-Type' : 'application/x-www-form-urlencoded'\n");
      out.write("          },\n");
      out.write("    })\n");
      out.write("    .then(response =>\n");
      out.write("    {\n");
      out.write("    return response.text();\n");
      out.write("    }\n");
      out.write("    ).then(data=> {\n");
      out.write("    const json = JSON.parse(data);\n");
      out.write("    document.getElementById(\"client_id\").value = json.client_id;\n");
      out.write("    document.getElementById(\"client_secret\").value =  json.client_secret;\n");
      out.write("    document.getElementById(\"redirect_uri\").value =  json.redirect_uri;\n");
      out.write("    //document.getElementById(\"scope\").value = \"ZohoPayout.Payouts.All\";\n");
      out.write("    }).catch(error => {\n");
      out.write("    alert(\"Invalid Password\");\n");
      out.write("    });\n");
      out.write("}\n");
      out.write("function resets()\n");
      out.write("{\n");
      out.write("    document.getElementById(\"populate\").style.display=\"block\";\n");
      out.write("    document.getElementById(\"client_id\").value=\"\";\n");
      out.write("    document.getElementById(\"scope\").value=\"\";\n");
      out.write("    document.getElementById(\"redirect_uri\").value=\"\";\n");
      out.write("    document.getElementById(\"client_secret\").value=\"\";\n");
      out.write("    document.getElementById(\"redirected_uri\").value=\"\";\n");
      out.write("    document.getElementById(\"response\").value=\"\";\n");
      out.write("    document.getElementById(\"refresh_token\").value=\"\";\n");
      out.write("\n");
      out.write("    document.getElementById(\"response\").style.display=\"none\";\n");
      out.write("}\n");
      out.write("function reset(auto)\n");
      out.write("{\n");
      out.write("    if(auto != true && !confirm(\"Are you sure you want to reset all values?\"))\n");
      out.write("    {\n");
      out.write("    return;\n");
      out.write("    }\n");
      out.write("    document.getElementById(\"client_id\").value=\"\";\n");
      out.write("    document.getElementById(\"scope\").value=\"\";\n");
      out.write("    document.getElementById(\"redirect_uri\").value=\"\";\n");
      out.write("    document.getElementById(\"client_secret\").value=\"\";\n");
      out.write("    document.getElementById(\"redirected_uri\").value=\"\";\n");
      out.write("    document.getElementById(\"response\").value=\"\";\n");
      out.write("    document.getElementById(\"refresh_token\").value=\"\";\n");
      out.write("\n");
      out.write("    document.getElementById(\"response\").style.display=\"none\";\n");
      out.write("\n");
      out.write("    if(!auto){\n");
      out.write("   const chbx = document.getElementsByName(\"dc\");\n");
      out.write("       for(let i=0; i < chbx.length; i++) {\n");
      out.write("       chbx[i].checked = false;\n");
      out.write("   }\n");
      out.write("   }\n");
      out.write("}\n");
      out.write("\n");
      out.write("</script>\n");
      out.write("<h1><b>Token Generator</b></h1>\n");
      out.write("<br/>\n");
      out.write("\n");
      out.write("<form>\n");
      out.write("  DC&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n");
      out.write("  <input type=\"radio\" id=\"dev\" name=\"dc\" value=\"dev\" onchange=\"populate()\">\n");
      out.write("  <label for=\"dev\">Development</label>\n");
      out.write("  <input type=\"radio\" id=\"local\" name=\"dc\" value=\"local\" onchange=\"populate()\">\n");
      out.write("  <label for=\"local\">Local</label>\n");
      out.write("  <input type=\"radio\" id=\"in\" name=\"dc\" value=\"in\" onchange=\"resets()\">\n");
      out.write("  <label for=\"in\">IN</label>\n");
      out.write("  <input type=\"radio\" id=\"us\" name=\"dc\" value=\"us\" onchange=\"resets()\">\n");
      out.write("  <label for=\"us\">US</label><br><br>\n");
      out.write("</form>\n");
      out.write("\n");
      out.write("Client ID &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" name=\"client_id\" id=\"client_id\" style=\"width:400px;height:25px\"><br><br>\n");
      out.write("Client Secret &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" name=\"client_secret\" id=\"client_secret\" style=\"width:400px;height:25px\"><br><br>\n");
      out.write("Scope   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" name=\"scope\" id=\"scope\" style=\"width:400px;height:25px\"><br><br>\n");
      out.write("Redirect URI  &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" name=\"redirect_uri\" id=\"redirect_uri\" style=\"width:400px;height:25px\"><br><br>\n");
      out.write("Refresh Token &nbsp;&nbsp;&nbsp;<input type=\"text\" id=\"refresh_token\" name=\"refresh_token\" style=\"width:400px;height:25px\"><br><br>\n");
      out.write("Redirected URI  &nbsp;<input type=\"text\" id=\"redirected_uri\" name=\"redirected_uri\" style=\"width:400px;height:25px\"><br><br>\n");
      out.write("\n");
      out.write("<button onclick=\"reset(false)\"> Reset </button>&nbsp;&nbsp;<button onclick=\"populate()\" style=\"display:none\" id=\"populate\"> Populate Credentials </button><br><br>\n");
      out.write("\n");
      out.write("<div id=\"response\" style=\"display:none;\">\n");
      out.write("<button onclick=\"copyAT()\"> Copy Access Token </button> <button onclick=\"copyRT()\"> Copy Refresh Token </button> <button onclick=\"copyAll()\"> Copy All </button> <br>\n");
      out.write("<textarea readonly id=\"output\" name=\"output\" rows=\"10\" cols=\"100\" style=\"font-size:18px;color:white;background-color:black \"></textarea>\n");
      out.write("</div>\n");
      out.write("<div style=\"background-color:#D3D3D3\">\n");
      out.write("<u><h2><b style=\"color:green;\">Access and Refresh token generation</b></h2></u>\n");
      out.write("<h3><b>Step 1: Code Generation (<spam style=\"font-size:15px;color:blue;\">Mandatory Fields :</spam><spam style=\"font-size:15px;color:red;\"> Client ID, Scope, Redirect URI</spam>)</b></h3>\n");
      out.write("<button onclick=\"redirect()\" title=\"You will be redirected to a consent page now. After giving consent, you will be redirected to another page and make sure to copy the the url of that page and paste it in Redirected URI field.\"> Get Code </button> <br>\n");
      out.write("\n");
      out.write("<h3><b>Step 2: Token Generation (<spam style=\"font-size:15px;color:blue;\">Mandatory Fields :</spam><spam style=\"font-size:15px;color:red;\"> Client ID, Client Secret, Redirect URI, Redirected URI</spam>)</b></h3>\n");
      out.write("<button id=\"tokenButton\" name=\"tokenButton\" onclick=\"getTokens()\"> Get Tokens </button> <br><br>\n");
      out.write("</div>\n");
      out.write("\n");
      out.write("<div style=\"background-color:#D3D3D3\">\n");
      out.write("<h2><b><u style=\"color:green;\">Generate Access token from Refresh token</u> (<spam style=\"font-size:15px;color:blue;\">Mandatory Fields : </spam><spam style=\"font-size:15px;color:red;\"> Client ID, Client Secret, Refresh Token</spam>)</b></h2></u>\n");
      out.write("<button onclick=\"refresh()\"> Generate Access Token </button> <br><br><br><br>\n");
      out.write("</div>\n");
      out.write("\n");
      out.write("</body>\n");
      out.write("</html>\n");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try {
            if (response.isCommitted()) {
              out.flush();
            } else {
              out.clearBuffer();
            }
          } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
