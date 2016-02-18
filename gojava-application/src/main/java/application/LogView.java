package application;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Servlet implementation class LogView
 */
@WebServlet("/LogView")
public class LogView extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogView() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void listFilesInDir(PrintWriter out, String dir, String prefix) {
        File f = new File(dir);
        if (f.list() != null) {
            long count = 0;
            for (String c : f.list()) {
                File cf = new File(f, c);
                if (cf.isDirectory()) {
                    out.println(" - " + prefix + count + " - " + c + " (dir)<br>");
                } else {
                    out.println(" - " + prefix + count + " - <a href=\"?cmd=view&choice=" + prefix + count + "\">" + c
                            + "</a><br>");
                }
                count++;
            }
        } else {
            System.out.println(" - Is empty, or not a directory." + "<br>");
        }
    }

    public void viewFile(PrintWriter out, String dir, String countString) {
        File f = new File(dir);
        if (f.list() != null) {
            long count = 0;
            for (String c : f.list()) {
                if (countString.equals("" + count)) {
                    System.out.println(
                            "LOGVIEW: Asked to view " + dir + " " + countString + " " + Paths.get(dir, c).toString());
                    try (Stream<String> stream = Files.lines(Paths.get(dir, c))) {
                        stream.forEach(out::println);
                    } catch (IOException io) {
                        out.println("ERROR READING FILE " + c + " " + io.getMessage());
                    }
                }
                count++;
            }
        } else {
            out.println("Directory '"+dir+"'does not exist to view file from.");
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.getDecoder().decode(st.nextToken()));
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String login = credentials.substring(0, p).trim();
                            String password = credentials.substring(p + 1).trim();

                            String expectedPassword="LOGLOGLOG";

                            if ("admin".equals(login) && expectedPassword.equals(password)) {
                                
                                String cmd = request.getParameter("cmd");
                                PrintWriter out = response.getWriter();
                                
                                if ("list".equals(cmd)) {
                                    response.addHeader("Content-Type", MediaType.TEXT_HTML);
                                    String outdir = System.getenv("WLP_OUTPUT_DIR");
                                    out.println("WLP_OUTPUT_DIR: " + String.valueOf(outdir) + "<br>");
                                    if (outdir != null) {
                                        listFilesInDir(out, outdir, "o");
                                    }
                                    String logdir = System.getenv("X_LOG_DIR");
                                    if (logdir != null) {
                                        out.println("X_LOG_DIR: " + String.valueOf(logdir) + "<br>");
                                        listFilesInDir(out, logdir, "l");

                                        String ffdcDir = new File(new File(logdir), "ffdc").getAbsolutePath();
                                        out.println("FFDC_DIR: " + String.valueOf(ffdcDir) + "<br>");
                                        listFilesInDir(out, ffdcDir, "f");
                                    } else {
                                        // going to try default location..
                                        out.println("LOG_DIR set as WLP_OUTPUT_DIR/defaultServer/logs" + "<br>");
                                        logdir = Paths.get(outdir, "defaultServer", "logs").toString();
                                        listFilesInDir(out, logdir, "l");

                                        String ffdcDir = new File(new File(logdir), "ffdc").getAbsolutePath();
                                        out.println("FFDC_DIR: " + String.valueOf(ffdcDir) + "<br>");
                                        listFilesInDir(out, ffdcDir, "f");
                                    }
                                } else if ("view".equals(cmd)) {
                                    response.addHeader("Content-Type", MediaType.TEXT_PLAIN);
                                    String choice = request.getParameter("choice");
                                    if (choice != null) {
                                        if (choice.startsWith("o")) {
                                            String outdir = System.getenv("WLP_OUTPUT_DIR");
                                            viewFile(out, outdir, choice.substring(1).trim());
                                        } else if (choice.startsWith("l")) {
                                            String logdir = System.getenv("X_LOG_DIR");
                                            if (logdir == null) {
                                                String outdir = System.getenv("WLP_OUTPUT_DIR");
                                                logdir = Paths.get(outdir, "defaultServer", "logs").toString();
                                            }
                                            viewFile(out, logdir, choice.substring(1).trim());
                                        } else if (choice.startsWith("f")) {
                                            String logdir = System.getenv("X_LOG_DIR");
                                            if (logdir == null) {
                                                String outdir = System.getenv("WLP_OUTPUT_DIR");
                                                logdir = Paths.get(outdir, "defaultServer", "logs").toString();
                                            }
                                            String ffdcDir = new File(new File(logdir), "ffdc").getAbsolutePath();
                                            viewFile(out, ffdcDir, choice.substring(1).trim());
                                        }
                                    } else {
                                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
                                                "view cmd requires choice param");
                                    }
                                } else {
                                    response.addHeader("Content-Type", MediaType.TEXT_HTML);
                                    out.println("<center><h1>Welcome to LogView.</h1></center>"
                                            + "<center>Your friendly logging choice.</center><hr><p><p><center>This logging console is shoeware, you may use it, but you must buy Ozzy shoes.</center><p><p>");
                                    out.println("<center><a href=\"?cmd=list\">Take me to the logs!!... </a></center>");
									String vcapapp = System.getenv("VCAP_APPLICATION");
									if(vcapapp!=null){
										out.println("Vcap:   "+vcapapp);
									}else{
										out.println("No vcap");
									}
                                }
                            }
                        } else {
                            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "badly formed auth header.");
                            return;
                        }
                    } catch (UnsupportedEncodingException e) {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Error decoding auth");
                        return;
                    }
                }
            }
        } else {
            response.addHeader("WWW-Authenticate", "Basic realm=\"Ozzy LogView\"");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access denied");
            return;
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

}