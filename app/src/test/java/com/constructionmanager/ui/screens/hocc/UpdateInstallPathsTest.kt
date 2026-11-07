package com.constructionmanager.ui.screens.hocc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** "Install it now" can only hand Android a file that sits under a root in res/xml/file_paths.xml (Nov 7 2026). */
class UpdateInstallPathsTest {
    private fun cachePaths(): List<String> {
        val f = listOf("src/main/res/xml/file_paths.xml", "app/src/main/res/xml/file_paths.xml").map(::File).first { it.exists() }
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f)
        val nodes = doc.getElementsByTagName("cache-path")
        return (0 until nodes.length).map { nodes.item(it).attributes.getNamedItem("path").nodeValue }
    }

    @Test fun theDownloadedBuildSitsUnderASharedCacheRoot() {
        val rel = UpdateInstall.DIR + "/" + UpdateInstall.FILE
        val roots = cachePaths().map { it.trimEnd('/') + "/" }
        assertTrue("$rel is under none of the shared cache roots $roots", roots.any { it == "./" || rel.startsWith(it) })
    }
}
