package com.example.monopoly_deal_game.journal;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 《5.9版本介绍》文档入口：解析路径并支持从模块类路径读取 Markdown 正文。
 */
public final class Banben59Jieshao {

    /** 与 {@code src/main/resources} 下相对路径一致（含中文文件名）。 */
    public static final String RESOURCE_PATH = "com/example/monopoly_deal_game/journal/5.9版本介绍.md";

    private Banben59Jieshao() {}

    /** @return 资源 {@link InputStream}，若不存在则返回 {@code null} */
    public static InputStream openStream() {
        return Banben59Jieshao.class.getResourceAsStream("/" + RESOURCE_PATH);
    }

    /** 将整个 Markdown 以 UTF-8 读成字符串（适合作业提交时随代码打印）。 */
    public static String readAllText() {
        try (InputStream in = openStream()) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    /** 供 IDE 或其它工具一键打开文档位置提示。 */
    public static String resourceHint() {
        return "classpath:/" + RESOURCE_PATH;
    }
}
