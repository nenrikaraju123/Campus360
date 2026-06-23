package com.campus360.platform.mail;

public class EmailTemplateBuilder {

    public static String build(String preheader, String title, String mainContent, String callToActionUrl, String callToActionText) {
        String btnHtml = "";
        if (callToActionUrl != null && !callToActionUrl.isEmpty()) {
            btnHtml = """
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; box-sizing: border-box; width: 100%%; min-width: 100%%;" width="100%%">
                      <tbody>
                        <tr>
                          <td align="left" style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top; padding-bottom: 24px; padding-top: 16px;" valign="top">
                            <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: auto;">
                              <tbody>
                                <tr>
                                  <td style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top; border-radius: 8px; text-align: center; background-color: #2563eb;" valign="top" align="center" bgcolor="#2563eb">
                                    <a href="%s" target="_blank" style="border: solid 1px #2563eb; border-radius: 8px; box-sizing: border-box; cursor: pointer; display: inline-block; font-size: 16px; font-weight: 600; margin: 0; padding: 14px 28px; text-decoration: none; text-transform: capitalize; background-color: #2563eb; border-color: #2563eb; color: #ffffff;">%s</a>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                    """.formatted(callToActionUrl, callToActionText);
        }

        return """
<!doctype html>
<html>
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>%s</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
@media only screen and (max-width: 620px) {
  table.body h1 {
    font-size: 28px !important;
    margin-bottom: 10px !important;
  }
  table.body p,
  table.body ul,
  table.body ol,
  table.body td,
  table.body span,
  table.body a {
    font-size: 16px !important;
  }
  table.body .wrapper,
  table.body .article {
    padding: 10px !important;
  }
  table.body .content {
    padding: 0 !important;
  }
  table.body .container {
    padding: 0 !important;
    width: 100%% !important;
  }
  table.body .main {
    border-left-width: 0 !important;
    border-radius: 0 !important;
    border-right-width: 0 !important;
  }
  table.body .btn table {
    width: 100%% !important;
  }
  table.body .btn a {
    width: 100%% !important;
  }
  table.body .img-responsive {
    height: auto !important;
    max-width: 100%% !important;
    width: auto !important;
  }
}
@media all {
  .ExternalClass {
    width: 100%%;
  }
  .ExternalClass,
  .ExternalClass p,
  .ExternalClass span,
  .ExternalClass font,
  .ExternalClass td,
  .ExternalClass div {
    line-height: 100%%;
  }
  .apple-link a {
    color: inherit !important;
    font-family: inherit !important;
    font-size: inherit !important;
    font-weight: inherit !important;
    line-height: inherit !important;
    text-decoration: none !important;
  }
  #MessageViewBody a {
    color: inherit;
    text-decoration: none;
    font-size: inherit;
    font-family: inherit;
    font-weight: inherit;
    line-height: inherit;
  }
  .btn-primary table td:hover {
    background-color: #1d4ed8 !important;
  }
  .btn-primary a:hover {
    background-color: #1d4ed8 !important;
    border-color: #1d4ed8 !important;
  }
}
</style>
  </head>
  <body class="" style="background-color: #f8fafc; font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; font-size: 16px; line-height: 1.6; margin: 0; padding: 0; -ms-text-size-adjust: 100%%; -webkit-text-size-adjust: 100%%;">
    <span class="preheader" style="color: transparent; display: none; height: 0; max-height: 0; max-width: 0; opacity: 0; overflow: hidden; mso-hide: all; visibility: hidden; width: 0;">%s</span>
    <table role="presentation" border="0" cellpadding="0" cellspacing="0" class="body" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #f8fafc; width: 100%%;" width="100%%" bgcolor="#f8fafc">
      <tr>
        <td style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top;" valign="top">&nbsp;</td>
        <td class="container" style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top; display: block; max-width: 600px; padding: 32px 20px; width: 600px; margin: 0 auto;" width="600" valign="top">
          <div class="header" style="padding: 0 0 24px; text-align: center; width: 100%%;">
            <h1 style="color: #0f172a; font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-weight: 700; line-height: 1.2; margin: 0; font-size: 28px; letter-spacing: -0.5px; text-align: center;">Campus360</h1>
          </div>
          <div class="content" style="box-sizing: border-box; display: block; margin: 0 auto; max-width: 600px; padding: 0;">

            <!-- START CENTERED WHITE CONTAINER -->
            <table role="presentation" class="main" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background: #ffffff; border-radius: 16px; width: 100%%; border: 1px solid #e2e8f0; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -4px rgba(0, 0, 0, 0.05);" width="100%%">

              <!-- START MAIN CONTENT AREA -->
              <tr>
                <td class="wrapper" style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top; box-sizing: border-box; padding: 40px;" valign="top">
                  <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%%;" width="100%%">
                    <tr>
                      <td style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top;" valign="top">
                        <h2 style="color: #0f172a; font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-weight: 600; line-height: 1.3; margin: 0 0 24px; font-size: 20px;">%s</h2>
                        %s
                        %s
                        <div style="margin-top: 40px; border-top: 1px solid #e2e8f0; padding-top: 24px;">
                          <p style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 15px; font-weight: normal; margin: 0; color: #475569; line-height: 1.6;">Best regards,<br><strong style="color: #0f172a; font-weight: 600;">The Campus360 Team</strong></p>
                        </div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>

            <!-- END MAIN CONTENT AREA -->
            </table>
            <!-- END CENTERED WHITE CONTAINER -->

            <!-- START FOOTER -->
            <div class="footer" style="clear: both; margin-top: 24px; text-align: center; width: 100%%;">
              <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%%;" width="100%%">
                <tr>
                  <td class="content-block" style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; vertical-align: top; padding-bottom: 10px; padding-top: 10px; color: #64748b; font-size: 13px; text-align: center; line-height: 1.5;" valign="top" align="center">
                    <span class="apple-link" style="color: #64748b; font-size: 13px; text-align: center; font-weight: 500;">Campus360 Inc, Enterprise Academic Solutions</span>
                    <br> You are receiving this email because of your registration or account creation with Campus360.
                  </td>
                </tr>
              </table>
            </div>
            <!-- END FOOTER -->

          </div>
        </td>
        <td style="font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; vertical-align: top;" valign="top">&nbsp;</td>
      </tr>
    </table>
  </body>
</html>
""".formatted(title, preheader, title, mainContent, btnHtml);
    }
}
