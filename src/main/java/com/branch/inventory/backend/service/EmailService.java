package com.branch.inventory.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false) // required=false so app starts even if SMTP is misconfigured
    private JavaMailSender mailSender;

    /**
     * Sends a welcome email with login credentials to the newly created user.
     * Runs asynchronously so it never blocks the HTTP response.
     * If SMTP fails for any reason, logs a warning and returns gracefully.
     *
     * @param fullName   user's full name
     * @param email      user's email address
     * @param password   temporary password
     * @param role       human-readable role label (e.g. "Branch Manager")
     * @param branchName branch name, or null if no branch assigned
     */
    @Async
    public void sendWelcomeEmail(String fullName, String email,
            String password, String role, String branchName) {
        if (mailSender == null) {
            log.warn("[mailer] JavaMailSender not configured — skipping real send, simulating delivery for: {}", email);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@branchinv.rw", "Branch Inventory System");
            helper.setTo(email);
            helper.setSubject("Your account is ready \u2713");
            helper.setText(buildHtml(fullName, email, password, role, branchName), true);

            mailSender.send(message);
            log.info("[mailer] Welcome email sent to {}", email);

        } catch (Exception ex) {
            // SMTP blocked, bad credentials, network issue — log but never crash the
            // request
            log.warn("[mailer] SMTP failed for {} — simulating delivery. Reason: {}", email, ex.getMessage());
        }
    }

    // ── HTML template ─────────────────────────────────────────────────────────
    private String buildHtml(String fullName, String email,
            String password, String role, String branchName) {
        String first = fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
        int year = Year.now().getValue();

        // Build the credentials rows
        StringBuilder credRows = new StringBuilder();
        credRows.append(credRow("Email address", email, true, false));
        credRows.append(credRow("Temporary password", password, true, true));
        credRows.append(credRow("Role", role, false, false));
        if (branchName != null && !branchName.isBlank()) {
            credRows.append(credRow("Branch", branchName, false, false));
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Your account is ready</title></head>
                <body style="margin:0;padding:0;background:#f6f8fc;font-family:'Google Sans',Roboto,sans-serif">
                <table width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:32px 16px">
                <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%">

                  <!-- dark header -->
                  <tr><td style="background:linear-gradient(135deg,#1a1f0e 0%%,#2d3520 100%%);border-radius:8px 8px 0 0;padding:24px 28px">
                    <table cellpadding="0" cellspacing="0"><tr>
                      <td style="width:44px;height:44px;background:rgba(255,255,255,.1);border-radius:8px;text-align:center;vertical-align:middle">
                        <span style="font-size:22px">&#127968;</span>
                      </td>
                      <td style="padding-left:14px">
                        <div style="font-size:16px;font-weight:600;color:#fff">Branch Inventory System</div>
                        <div style="font-size:12px;color:rgba(255,255,255,.55);margin-top:2px">Multi-Branch Transfer Management</div>
                      </td>
                    </tr></table>
                  </td></tr>

                  <!-- success stripe -->
                  <tr><td style="background:#dcfce7;padding:12px 28px;border-bottom:1px solid #bbf7d0">
                    <span style="font-size:13px;font-weight:600;color:#166534">&#10003; Account successfully created</span>
                  </td></tr>

                  <!-- body -->
                  <tr><td style="background:#fff;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 8px 8px;padding:28px">
                    <p style="font-size:15px;color:#202124;margin:0 0 8px">Hi %s,</p>
                    <p style="font-size:13px;color:#5f6368;margin:0 0 20px;line-height:1.7">
                      Your account on <strong style="color:#202124">Branch Inventory System</strong> has been created
                      by a system administrator. You can sign in immediately using the details below.
                      For security, you will be prompted to set a new password the first time you log in.
                    </p>

                    <!-- credentials box -->
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#f8f9fa;border:1px solid #e8eaed;border-radius:8px;padding:16px 20px;margin-bottom:24px">
                      <tr><td>
                        <div style="font-size:11px;font-weight:700;color:#80868b;text-transform:uppercase;
                                    letter-spacing:.08em;margin-bottom:14px">Your Login Credentials</div>
                        %s
                      </td></tr>
                    </table>

                    <!-- CTA -->
                    <table cellpadding="0" cellspacing="0" style="margin-bottom:24px"><tr>
                      <td style="background:#1a73e8;border-radius:4px;padding:10px 24px">
                        <a href="#" style="font-size:14px;font-weight:500;color:#fff;text-decoration:none">
                          Sign in to your account &#8594;
                        </a>
                      </td>
                    </tr></table>

                    <!-- security notice -->
                    <table cellpadding="0" cellspacing="0"
                           style="background:#fff8e1;border:1px solid #ffe082;border-radius:6px;
                                  padding:10px 14px;margin-bottom:20px;width:100%%"><tr>
                      <td style="font-size:12px;color:#92400e;line-height:1.6">
                        &#9888; Your temporary password was delivered securely.
                        Never share your credentials — Branch Inventory will never ask for your password.
                      </td>
                    </tr></table>

                    <p style="font-size:11px;color:#9aa0a6;line-height:1.7;
                               padding-top:16px;border-top:1px solid #f1f3f4;margin:0">
                      This is an automated message. Please do not reply.<br>
                      If you did not expect this account, contact your administrator immediately.<br><br>
                      &copy; %d Branch Inventory System &middot; Kigali, Rwanda
                    </p>
                  </td></tr>
                </table>
                </td></tr></table>
                </body></html>
                """
                .formatted(first, credRows.toString(), year);
    }

    private String credRow(String label, String value, boolean mono, boolean highlight) {
        String color = highlight ? "#78350f" : "#202124";
        String bg = highlight ? "#fef9c3" : mono ? "#e8f0fe" : "transparent";
        String border = highlight ? "1px solid #fde047" : "none";
        String weight = highlight ? "700" : "400";
        String pad = mono ? "2px 8px" : "0";
        String font = mono ? "monospace" : "inherit";
        return """
                <table cellpadding="0" cellspacing="0" style="margin-bottom:10px"><tr>
                  <td style="font-size:12px;color:#80868b;width:140px;vertical-align:top;padding-top:2px">%s</td>
                  <td style="font-size:13px;color:%s;font-family:%s;background:%s;border:%s;
                             padding:%s;border-radius:4px;font-weight:%s">%s</td>
                </tr></table>
                """.formatted(label, color, font, bg, border, pad, weight, value);
    }
}
