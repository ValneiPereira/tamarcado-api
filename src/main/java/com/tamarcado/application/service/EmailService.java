package com.tamarcado.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void sendPasswordResetCode(String toEmail, String userName, String code) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Tá Marcado! - Código para redefinir sua senha");
            helper.setText(buildResetEmailHtml(userName, code), true);

            mailSender.send(message);
            log.info("E-mail de redefinição enviado para {}", toEmail);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de redefinição para {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Erro ao enviar e-mail. Tente novamente.");
        }
    }

    private String buildResetEmailHtml(String userName, String code) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px;">
                  <div style="max-width: 480px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #1E3A8A; padding: 24px; text-align: center;">
                      <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Tá Marcado!</h1>
                    </div>
                    <div style="padding: 32px 24px;">
                      <p style="color: #333; font-size: 16px;">Olá, <strong>%s</strong>!</p>
                      <p style="color: #555; font-size: 15px;">
                        Recebemos uma solicitação para redefinir a senha da sua conta.
                        Use o código abaixo no app para criar uma nova senha:
                      </p>
                      <div style="background: #f0f4ff; border: 2px solid #1E3A8A; border-radius: 8px;
                                  padding: 20px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #1E3A8A;">%s</span>
                      </div>
                      <p style="color: #888; font-size: 13px;">
                        Este código expira em <strong>15 minutos</strong>.<br>
                        Se você não solicitou a redefinição, ignore este e-mail.
                      </p>
                    </div>
                    <div style="background: #f5f5f5; padding: 16px; text-align: center;">
                      <p style="color: #aaa; font-size: 12px; margin: 0;">© 2025 Tá Marcado! - Todos os direitos reservados</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(userName, code);
    }
}
