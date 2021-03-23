/*
 * ForgeGradle
 * Copyright (C) 2018 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.gradle.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.gradle.api.Project;

public class MojangLicenseHelper {

    private static boolean shown = false;

    @Deprecated
    @SuppressWarnings("unused")
    public static void displayWarning(Project project, String channel) {
        if ("official".equals(channel)) {
            displayWarning(project, (File) null);
        }
    }

    //TODO: Add a task that people can run to quiet this warning.
    //Also output the specific text from the targeted MC version.
    public static void displayWarning(Project project, @Nullable File proguard) {
        if (!shown) {
            shown = true;

            String license = proguard == null ? null : getLicense(proguard);

            String warning = ("WARNING: "
                + "This project is configured to use the official obfuscation mappings provided by Mojang. "
                + "These mapping fall under their associated license, you should be fully aware of this license. "
                + "For the relevant license text, refer {REFER}, or the reference copy here: "
                + "https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md").replace("{REFER}",
                license != null ? "below" : "to the mapping file itself");
            project.getLogger().warn(warning);

            if (license != null) project.getLogger().warn("License: " + license);
        }
    }

    private static String getLicense(File proguard) {
        try {
            return Files.lines(proguard.toPath())
                .filter(line -> line.startsWith("#"))
                .map(l -> l.substring(1))
                .collect(Collectors.joining("\n"));
        } catch (IOException ignored) {
            return null;
        }
    }
}
