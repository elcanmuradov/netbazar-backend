package com.swaply.notificationservice.utils.constants;

import com.swaply.notificationservice.dto.ProductDeletedEmailRequest;
import com.swaply.notificationservice.dto.TicketDto;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EmailContext {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId BAKU_ZONE = ZoneId.of("Asia/Baku");
    private static final DateTimeFormatter EMAIL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy  -  HH:mm");

    public static String setToken(String token){
        return String.format("""
            <!DOCTYPE html>
            <html lang="az">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Swaply - Email Təsdiqi</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
            
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #F0F0F0;
                        padding: 40px 20px;
                    }
            
                    .email-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #FEFEFE;
                        border-radius: 12px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
            
                    .header {
                        background-color: #113E21;
                        padding: 40px 30px;
                        text-align: center;
                    }
            
                    .logo {
                        font-size: 32px;
                        font-weight: 700;
                        color: #FEFEFE;
                        letter-spacing: 2px;
                    }
            
                    .logo span {
                        color: #B38B59;
                    }
            
                    .content {
                        padding: 50px 40px;
                    }
            
                    .greeting {
                        font-size: 24px;
                        color: #1A1A1A;
                        margin-bottom: 20px;
                        text-align: center;
                    }
            
                    .message {
                        font-size: 16px;
                        color: #666666;
                        line-height: 1.6;
                        text-align: center;
                        margin-bottom: 30px;
                    }
            
                    .verification-code {
                        background-color: #F0F0F0;
                        border: 2px dashed #113E21;
                        border-radius: 8px;
                        padding: 25px;
                        text-align: center;
                        margin: 30px 0;
                    }
            
                    .code {
                        font-size: 36px;
                        font-weight: 700;
                        color: #113E21;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
            
                    .code-label {
                        font-size: 14px;
                        color: #666666;
                        margin-bottom: 10px;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
            
                    .btn {
                        display: inline-block;
                        background-color: #113E21;
                        color: #FEFEFE;
                        text-decoration: none;
                        padding: 16px 40px;
                        border-radius: 8px;
                        font-size: 16px;
                        font-weight: 600;
                        margin: 20px 0;
                        transition: background-color 0.3s;
                    }
            
                    .btn:hover {
                        background-color: #0d2f19;
                    }
            
                    .divider {
                        height: 1px;
                        background-color: #F0F0F0;
                        margin: 30px 0;
                    }
            
                    .footer {
                        background-color: #F0F0F0;
                        padding: 30px 40px;
                        text-align: center;
                    }
            
                    .footer-text {
                        font-size: 14px;
                        color: #666666;
                        line-height: 1.6;
                    }
            
                    .footer-text a {
                        color: #113E21;
                        text-decoration: none;
                    }
            
                    .expiry {
                        font-size: 13px;
                        color: #666666;
                        margin-top: 15px;
                        font-style: italic;
                    }
            
                    .social-links {
                        margin-top: 20px;
                    }
            
                    .social-links a {
                        display: inline-block;
                        margin: 0 10px;
                        color: #113E21;
                        text-decoration: none;
                        font-size: 14px;
                    }
            
                    @media only screen and (max-width: 600px) {
                        .content {
                            padding: 30px 20px;
                        }
            
                        .greeting {
                            font-size: 20px;
                        }
            
                        .code {
                            font-size: 28px;
                            letter-spacing: 5px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <!-- Header -->
                    <div class="header">
                        <div class="logo">Swap<span>ly</span></div>
                    </div>
            
                    <!-- Content -->
                    <div class="content">
                        <h1 class="greeting">Emailinizi Təsdiqləyin</h1>
            
                        <p class="message">
                            Salam! Swaply hesabınızı yaratdığınız üçün təşəkkür edirik.\s
                            Hesabınızı aktivləşdirmək üçün aşağıdakı təsdiq kodundan istifadə edin.
                        </p>
            
                        <!-- Verification Code -->
                        <div class="verification-code">
                            <div class="code-label">Təsdiq Kodu</div>
                            <div class="code">%s</div>
                            <div class="expiry">⏱ Kod 2 dəqiqə etibarlıdır</div>
                        </div>
            
                      
            
                        <div class="divider"></div>
            
                        <p class="message" style="font-size: 14px;">
                            Əgər bu düymə işləmirsə, aşağıdakı linki kopyalayıb brauzerinizə yapışdırın:
                            <br><br>
                            <strong style="color: #113E21; word-break: break-all;">{{VERIFICATION_LINK}}</strong>
                        </p>
                    </div>
            
                    <!-- Footer -->
                    <div class="footer">
                        <p class="footer-text">
                            Bu emaili <strong>Swaply</strong> komandası göndərdi.
                            <br>
                            Əgər bu qeydiyyatı siz etməmisinizsə, bu emaili nəzərə almayın.
                        </p>
            
                        <div class="social-links">
                            <a href="www.swaply.dev">Website</a> |\s
                            <a href="www.swaply.dev/support">Support</a> |\s
                            <a href="#">Privacy Policy</a>
                        </div>
            
                        <p class="footer-text" style="margin-top: 20px; font-size: 12px;">
                            © 2026 Swaply. Bütün hüquqlar qorunur.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,token);
    }



    public static String setDeletedProductNotification(ProductDeletedEmailRequest request) {
        String productTitle = escapeHtml(defaultIfBlank(request.productTitle, "Məhsul"));
        String reason = escapeHtml(defaultIfBlank(request.reason, "Qayda pozuntusu"));

        return """
                <!DOCTYPE html>
                <html lang="az">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Swaply - Məhsul Silindi</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }

                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #F0F0F0;
                            padding: 40px 20px;
                        }

                        .email-container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #FEFEFE;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                        }

                        .header {
                            background-color: #113E21;
                            padding: 40px 30px;
                            text-align: center;
                        }

                        .logo {
                            font-size: 32px;
                            font-weight: 700;
                            color: #FEFEFE;
                            letter-spacing: 2px;
                        }

                        .logo span {
                            color: #B38B59;
                        }

                        .warning-icon {
                            font-size: 48px;
                            margin-bottom: 15px;
                        }

                        .content {
                            padding: 50px 40px;
                        }

                        .greeting {
                            font-size: 24px;
                            color: #1A1A1A;
                            margin-bottom: 20px;
                            text-align: center;
                        }

                        .message {
                            font-size: 16px;
                            color: #666666;
                            line-height: 1.6;
                            text-align: center;
                            margin-bottom: 30px;
                        }

                        .alert-box {
                            background-color: #FFF3CD;
                            border-left: 4px solid #B38B59;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 30px 0;
                            text-align: left;
                        }

                        .alert-title {
                            font-size: 16px;
                            font-weight: 700;
                            color: #113E21;
                            margin-bottom: 10px;
                            display: flex;
                            align-items: center;
                            gap: 8px;
                        }

                        .alert-content {
                            font-size: 14px;
                            color: #666666;
                            line-height: 1.6;
                        }

                        .product-info {
                            background-color: #F0F0F0;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 30px 0;
                        }

                        .product-info-title {
                            font-size: 14px;
                            color: #666666;
                            margin-bottom: 10px;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                        }

                        .product-detail {
                            font-size: 16px;
                            color: #1A1A1A;
                            font-weight: 600;
                            margin-bottom: 8px;
                        }

                        .product-detail span {
                            font-weight: 400;
                            color: #666666;
                        }

                        .btn {
                            display: inline-block;
                            background-color: #113E21;
                            color: #FEFEFE;
                            text-decoration: none;
                            padding: 16px 40px;
                            border-radius: 8px;
                            font-size: 16px;
                            font-weight: 600;
                            margin: 20px 0;
                            transition: background-color 0.3s;
                        }

                        .btn:hover {
                            background-color: #0d2f19;
                        }

                        .divider {
                            height: 1px;
                            background-color: #F0F0F0;
                            margin: 30px 0;
                        }

                        .warning-section {
                            background-color: #F0F0F0;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 30px 0;
                            text-align: center;
                        }

                        .warning-title {
                            font-size: 16px;
                            font-weight: 700;
                            color: #113E21;
                            margin-bottom: 10px;
                        }

                        .warning-text {
                            font-size: 14px;
                            color: #666666;
                            line-height: 1.6;
                        }

                        .footer {
                            background-color: #F0F0F0;
                            padding: 30px 40px;
                            text-align: center;
                        }

                        .footer-text {
                            font-size: 14px;
                            color: #666666;
                            line-height: 1.6;
                        }

                        .social-links {
                            margin-top: 20px;
                        }

                        .social-links a {
                            display: inline-block;
                            margin: 0 10px;
                            color: #113E21;
                            text-decoration: none;
                            font-size: 14px;
                        }

                        @media only screen and (max-width: 600px) {
                            .content {
                                padding: 30px 20px;
                            }

                            .greeting {
                                font-size: 20px;
                            }

                            .alert-box {
                                padding: 15px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <div class="logo">Swap<span>ly</span></div>
                        </div>

                        <div class="content">
                            <div class="warning-icon">⚠️</div>

                            <h1 class="greeting">Məhsulunuz Silindi</h1>

                            <p class="message">
                                Salam,<br><br>
                                Təəssüf ki, məhsulunuz platforma qaydalarına uyğun olmadığı üçün admin tərəfindən silindi.
                            </p>

                            <div class="alert-box">
                                <div class="alert-title">📋 Silinmə Səbəbi</div>
                                <div class="alert-content">
                                    <strong>Admin</strong> tərəfindən <strong>%s</strong>-a görə məhsulunuz silindi.
                                </div>
                            </div>

                            <div class="product-info">
                                <div class="product-info-title">📦 Məhsul Məlumatları</div>
                                <div class="product-detail">Məhsul Adı: <span>%s</span></div>
                            </div>

                            <div class="warning-section">
                                <div class="warning-title">⚠️ Vacib Xəbərdarlıq</div>
                                <p class="warning-text">
                                    Gələcəkdə qayda pozuntuları təkrarlanarsa, hesabınız müvəqqəti və ya daimi olaraq məhdudlaşdırıla bilər.
                                </p>
                            </div>

                            <div class="divider"></div>

                            <p class="message" style="font-size: 14px; margin-bottom: 0;">
                                Əgər bu barədə sualınız varsa, dəstək komandamızla əlaqə saxlaya bilərsiniz.
                            </p>
                        </div>

                        <div class="footer">
                            <p class="footer-text">
                                Bu emaili <strong>Swaply</strong> komandası göndərdi.
                                <br>
                                Bu, avtomatik göndərilən bildiriş emailidir.
                            </p>

                            <div class="social-links">
                                <a href="www.swaply.dev">Website</a> |
                                <a href="www.swaply.dev/support">Support</a> |
                                <a href="#">Privacy Policy</a> |
                                <a href="#">Community Guidelines</a>
                            </div>

                            <p class="footer-text" style="margin-top: 20px; font-size: 12px;">
                                © 2026 Swaply. Bütün hüquqlar qorunur.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(reason, productTitle);
    }

    public static String setResponseReportMessage(TicketDto ticket) {
        String context = String.format("""
                        <!DOCTYPE html>
                        <html lang="az">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Swaply - Dəstək Cavabı</title>
                            <style>
                                * {
                                    margin: 0;
                                    padding: 0;
                                    box-sizing: border-box;
                                }
                        
                                body {
                                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                                    background-color: #F0F0F0;
                                    padding: 40px 20px;
                                }
                        
                                .email-container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    background-color: #FEFEFE;
                                    border-radius: 12px;
                                    overflow: hidden;
                                    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                                }
                        
                                .header {
                                    background-color: #113E21;
                                    padding: 40px 30px;
                                    text-align: center;
                                }
                        
                                .logo {
                                    font-size: 32px;
                                    font-weight: 700;
                                    color: #FEFEFE;
                                    letter-spacing: 2px;
                                }
                        
                                .logo span {
                                    color: #B38B59;
                                }
                        
                                .support-badge {
                                    display: inline-block;
                                    background-color: #B38B59;
                                    color: #FEFEFE;
                                    padding: 6px 16px;
                                    border-radius: 20px;
                                    font-size: 12px;
                                    font-weight: 600;
                                    margin-top: 15px;
                                    text-transform: uppercase;
                                    letter-spacing: 1px;
                                }
                        
                                .content {
                                    padding: 50px 40px;
                                }
                        
                                .greeting {
                                    font-size: 24px;
                                    color: #1A1A1A;
                                    margin-bottom: 20px;
                                    text-align: center;
                                }
                        
                                .message {
                                    font-size: 16px;
                                    color: #666666;
                                    line-height: 1.6;
                                    text-align: center;
                                    margin-bottom: 30px;
                                }
                        
                                .ticket-info {
                                    background-color: #F0F0F0;
                                    border-radius: 8px;
                                    padding: 20px;
                                    margin: 30px 0;
                                }
                        
                                .ticket-header {
                                    display: flex;
                                    justify-content: space-between;
                                    align-items: center;
                                    margin-bottom: 15px;
                                    flex-wrap: wrap;
                                    gap: 10px;
                                }
                        
                                .ticket-id {
                                    font-size: 14px;
                                    color: #666666;
                                    font-weight: 600;
                                }
                        
                                .ticket-id span {
                                    color: #113E21;
                                    font-family: 'Courier New', monospace;
                                }
                        
                                .status-badge {
                                    padding: 6px 14px;
                                    border-radius: 20px;
                                    font-size: 12px;
                                    font-weight: 600;
                                    text-transform: uppercase;
                                }
                        
                                .status-resolved {
                                    background-color: #D4EDDA;
                                    color: #155724;
                                }
                        
                                .status-pending {
                                    background-color: #FFF3CD;
                                    color: #856404;
                                }
                        
                                .status-in-progress {
                                    background-color: #CCE5FF;
                                    color: #004085;
                                }
                        
                                .ticket-detail {
                                    font-size: 14px;
                                    color: #1A1A1A;
                                    margin-bottom: 8px;
                                    display: flex;
                                    align-items: flex-start;
                                    gap: 10px;
                                }
                        
                                .ticket-detail-label {
                                    color: #666666;
                                    min-width: 100px;
                                    font-weight: 500;
                                }
                        
                                .ticket-detail-value {
                                    flex: 1;
                                    word-break: break-word;
                                }
                        
                                .divider {
                                    height: 1px;
                                    background-color: #F0F0F0;
                                    margin: 30px 0;
                                }
                        
                                .section-title {
                                    font-size: 16px;
                                    font-weight: 700;
                                    color: #113E21;
                                    margin-bottom: 15px;
                                    display: flex;
                                    align-items: center;
                                    gap: 8px;
                                }
                        
                                .message-box {
                                    background-color: #F0F0F0;
                                    border-left: 4px solid #113E21;
                                    border-radius: 8px;
                                    padding: 20px;
                                    margin-bottom: 25px;
                                }
                        
                                .message-header {
                                    display: flex;
                                    justify-content: space-between;
                                    align-items: center;
                                    margin-bottom: 12px;
                                    font-size: 13px;
                                    color: #666666;
                                }
                        
                                .message-sender {
                                    font-weight: 600;
                                    color: #1A1A1A;
                                }
                        
                                .message-date {
                                    color: #666666;
                                }
                        
                                .message-content {
                                    font-size: 15px;
                                    color: #1A1A1A;
                                    line-height: 1.6;
                                    white-space: pre-wrap;
                                }
                        
                                .user-message {
                                    border-left-color: #B38B59;
                                    background-color: #FFFBF0;
                                }
                        
                                .admin-message {
                                    border-left-color: #113E21;
                                    background-color: #F0F7F2;
                                }
                        
                                .admin-signature {
                                    margin-top: 20px;
                                    padding-top: 20px;
                                    border-top: 1px solid #F0F0F0;
                                }
                        
                                .admin-name {
                                    font-size: 16px;
                                    font-weight: 600;
                                    color: #1A1A1A;
                                }
                        
                                .admin-role {
                                    font-size: 14px;
                                    color: #666666;
                                }
                        
                                .btn {
                                    display: inline-block;
                                    background-color: #113E21;
                                    color: #FEFEFE;
                                    text-decoration: none;
                                    padding: 16px 40px;
                                    border-radius: 8px;
                                    font-size: 16px;
                                    font-weight: 600;
                                    margin: 20px 0;
                                    transition: background-color 0.3s;
                                }
                        
                                .btn:hover {
                                    background-color: #0d2f19;
                                }
                        
                                .btn-secondary {
                                    background-color: #FEFEFE;
                                    color: #113E21;
                                    border: 2px solid #113E21;
                                }
                        
                                .btn-secondary:hover {
                                    background-color: #F0F0F0;
                                }
                        
                                .btn-group {
                                    text-align: center;
                                    margin: 30px 0;
                                }
                        
                                .btn-group .btn {
                                    margin: 5px 10px;
                                }
                        
                                .footer {
                                    background-color: #F0F0F0;
                                    padding: 30px 40px;
                                    text-align: center;
                                }
                        
                                .footer-text {
                                    font-size: 14px;
                                    color: #666666;
                                    line-height: 1.6;
                                }
                        
                                .footer-text a {
                                    color: #113E21;
                                    text-decoration: none;
                                }
                        
                                .social-links {
                                    margin-top: 20px;
                                }
                        
                                .social-links a {
                                    display: inline-block;
                                    margin: 0 10px;
                                    color: #113E21;
                                    text-decoration: none;
                                    font-size: 14px;
                                }
                        
                                .help-tip {
                                    background-color: #F0F7F2;
                                    border-radius: 8px;
                                    padding: 15px;
                                    margin-top: 25px;
                                    text-align: center;
                                }
                        
                                .help-tip-text {
                                    font-size: 14px;
                                    color: #666666;
                                }
                        
                                .help-tip-text strong {
                                    color: #113E21;
                                }
                        
                                @media only screen and (max-width: 600px) {
                                    .content {
                                        padding: 30px 20px;
                                    }
                        
                                    .greeting {
                                        font-size: 20px;
                                    }
                        
                                    .ticket-header {
                                        flex-direction: column;
                                        align-items: flex-start;
                                    }
                        
                                    .btn-group .btn {
                                        display: block;
                                        margin: 10px 0;
                                    }
                                }
                            </style>
                        </head>
                <body>
                    <div class="email-container">
                        <!-- Header -->
                        <div class="header">
                            <div class="logo">Swap<span>ly</span></div>
                            <div class="support-badge">🎧 Dəstək Komandası</div>
                        </div>
                
                        <!-- Content -->
                        <div class="content">
                            <h1 class="greeting">Müraciətinizə Cavab Var</h1>
                
                            <p class="message">
                                Salam <strong>%s</strong>,<br><br>
                                Swaply Dəstək Komandası müraciətinizi nəzərdən keçirdi və aşağıda cavabını tapa bilərsiniz.
                            </p>
                
                            <!-- Ticket Info -->
                            <div class="ticket-info">
                                <div class="ticket-header">
                                    <div class="ticket-id">Ticket ID: <span>%s</span></div>
                                    <span class="status-badge status-resolved">RESOLVED</span>
                                </div>
                
                                <div class="ticket-detail">
                                    <span class="ticket-detail-label">📌 Mövzu:</span>
                                    <span class="ticket-detail-value">%s</span>
                                </div>
                                <div class="ticket-detail">
                                    <span class="ticket-detail-label">📅 Göndərilmə:</span>
                                    <span class="ticket-detail-value">%s</span>
                                </div>
                            </div>
                
                            <!-- User's Original Message -->
                            <div class="section-title">💬 Sizin Müraciətiniz</div>
                            <div class="message-box user-message">
                                <div class="message-header">
                                    <span class="message-sender">%s</span>
                                    <span class="message-date">%s</span>
                                </div>
                                <div class="message-content">%s</div>
                            </div>
                
                            <!-- Admin's Response -->
                            <div class="section-title">✅ Komandamızın Cavabı</div>
                            <div class="message-box admin-message">
                                <div class="message-header">
                                    <span class="message-sender">Swaply Dəstək</span>
                                    <span class="message-date">%s</span>
                                </div>
                                <div class="message-content">%s</div>
                            </div>
                
                            <!-- Admin Signature -->
                            <div class="admin-signature">
                                <div class="admin-name">Elcan Muradov</div>
                                <div class="admin-role">Elcan • Swaply Support Team</div>
                            </div>
                
                            <!-- Action Buttons -->
                            <div class="btn-group">
                                <a href="https://www.swaply.dev/support" class="btn">Müraciəti Bax</a>
                                <a href="https://www.swaply.dev/support" class="btn btn-secondary">Cavab Yaz</a>
                            </div>
                
                            <div class="divider"></div>
                
                            <!-- Help Tip -->
                            <div class="help-tip">
                                <p class="help-tip-text">
                                    💡 <strong>İpucu:</strong> Cavabımız kifayət deyilsə və ya əlavə sualınız varsa,
                                    yuxarıdakı "Cavab Yaz" düyməsinə klikləyərək birbaşa bu müraciətə cavab yaza bilərsiniz.
                                </p>
                            </div>
                
                            <p class="message" style="font-size: 14px; margin-top: 25px;">
                                Əlavə kömək lazımdırsa, birbaşa <strong>Admin</strong>-ə yaza bilərsiniz.
                            </p>
                        </div>
                
                        <!-- Footer -->
                        <div class="footer">
                            <p class="footer-text">
                                Bu emaili <strong>Swaply</strong> Dəstək Komandası göndərdi.
                                <br>
                                Bu, avtomatik göndərilən bildiriş emailidir. Zəhmət olmasa cavab verməyin.
                            </p>
                
                            <div class="social-links">
                                <a href="https://www.swaply.dev">Website</a> |
                                <a href="https://www.swaply.dev/support">Support</a> |
                                <a href="#">Privacy Policy</a> |
                                <a href="#">Knowledge Base</a>
                            </div>
                
                            <p class="footer-text" style="margin-top: 20px; font-size: 12px;">
                                © 2025 Swaply. Bütün hüquqlar qorunur.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """
                ,ticket.getUserName()
                ,ticket.getId()
                ,ticket.getTitle()
                ,ticket.getReportTime()
                ,ticket.getUserName()
                ,ticket.getReportTime()
                ,ticket.getUserReport()
                ,ticket.getResponseTime()
                ,ticket.getAdminResponse());

        return context;
    }

    private static String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

  
}
