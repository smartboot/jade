package org.smartboot.jade;

public class HomeTpl {
    public static byte[] html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Welcome to Jade</title>
                <meta name="viewport" content="width=device-width, initial-scale=1"> <!-- 确保适当的缩放和布局 -->
                        
                <style>        * {
                    box-sizing: border-box; /* 统一盒模型 */
                }
                        
                body {
                    margin: 0;
                    min-height: 100vh;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    align-items: center;
                    background: linear-gradient(90deg, #FF9A8B 0%, #FF6A88 100%);
                    animation: gradient 15s ease infinite;
                }
                        
                h1 {
                    font-family: 'Arial', sans-serif;
                    color: #FFFFFF;
                    font-size: 4em;
                    text-shadow: 0 0 10px rgba(255,255,255,0.5), 0 0 20px rgba(255,255,255,0.3);
                    animation: glow 2s ease-in-out infinite alternate;
                    margin-bottom: 40px; /* 添加底部外边距 */
                }
                        
                #usage-instructions {
                    max-width: 800px;
                    width: 100%;
                    padding: 30px;
                    background-color: rgba(255, 255, 255, 0.9);
                    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
                    border-radius: 10px;
                    text-align: left;
                    display: flex;
                    flex-direction: column;
                }
                        
                #usage-instructions h2 {
                    color: #FF6A88;
                    margin-bottom: 20px;
                    font-size: 2em;
                }
                        
                #usage-instructions li {
                    line-height: 1.6;
                    margin-bottom: 10px;
                    animation: float 2s ease-in-out infinite;
                }
                        
                @keyframes gradient {
                    /* ...原有渐变动画... */
                }
                        
                @keyframes glow {
                    /* ...原有文字发光动画... */
                }
                        
                @keyframes float {
                    /* ...原有浮动动画... */
                }
                        
                /* 响应式设计 */
                @media (max-width: 600px) {
                    h1 {
                        font-size: 3em; /* 在小屏幕上减小标题大小 */
                    }
                    #usage-instructions {
                        padding: 20px 10px; /* 减少内边距适应小屏幕 */
                    }
                }
                </style>
            </head>
            <body>
            <h1>Welcome to Jade.</h1>
            <!-- 添加产品使用说明的区块 -->
            <div id="usage-instructions">
                <h2>Product Usage Instructions:</h2>
                <ol>
                    <li>Step 1: First, ensure that your device is properly connected to the Jade application.</li>
                    <li>Step 2: Open the app and navigate to the 'Settings' tab to customize your preferences.</li>
                    <li>Step 3: Click on the 'Start' button to initiate the process. Monitor the progress in real-time on the dashboard.</li>
                    <li>Step 4: For advanced features, explore the 'Tools' section located in the sidebar menu.</li>
                    <li>Step 5: If you encounter any issues, refer to our comprehensive FAQ within the app or contact our support team.</li>
                </ol>
            </div>
            <!-- 添加GitHub仓库链接的段落 -->
            <section id="github-link">
                <p>For more information, source code, and contributions, visit our
                    <a href="https://github.com/smartboot/jade" target="_blank">GitHub Repository</a>.
                </p>
            </section>
            </body>
            </html>
            """.getBytes();

}
