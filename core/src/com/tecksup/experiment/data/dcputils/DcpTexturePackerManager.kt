package com.tecksup.experiment.data.dcputils

import com.badlogic.gdx.tools.texturepacker.TexturePacker
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.util.*

/** Created by Darius on 15-Feb-19. */
class DcpTexturePackerManager {
    /** Will use texturePacker to pack all images, only if files have changed or atlas output files are not created
     * Loads settings from pack.json files in folders
     * Gradle task texturePacker does the same packing, Android launcher won't pack so use the task if needed
     *
     * Images starting with _ are deleted automatically */
    fun checkWhetherToPackImages() {

        var exportAtlasFolder = "textureAtlas"
        var imagesOrigin = "../Images/atlas"
        var hashFileName = "textureHash.txt"

        //Atlas file location in a distro
        var imageOriginDistro = "images"

        if (File(imagesOrigin).isDirectory) {
            println("in editor packing")
            packImages(hashFileName, imagesOrigin, exportAtlasFolder)
        } else if (File(imageOriginDistro).isDirectory) {
            println("Distro packing")
            packImages(hashFileName, imageOriginDistro, exportAtlasFolder)
        } else {
            println("Failed to attempt texture packing")
            return
        }
    }

    fun packImages(FileName: String, origin: String, ExportFolder: String) {

        var hashFile = File(origin + "/" + FileName)

        println("_____\nChecking image hashes...")
        var initTime = System.currentTimeMillis()

        fun hashAllFiles(file: File, hashFile: File): Int {
            var total = 0
            if (file.isDirectory) {
                for (content in file.listFiles()) {
                    total += hashAllFiles(content, hashFile)
                }
            } else {
                if (file == hashFile) {
                    return 0
                }

                val input = FileInputStream(file.path)
                var c = 0
                while (c != -1) {
                    c = input.read()
                    total += c
                }
                input.close()
            }
            return total
        }

        var hashingTotal = hashAllFiles(File(origin), hashFile)
        var c = 0
        try {
            val atlasFile = File(ExportFolder + "/atlas.atlas")
            val atlasImageFile = File(ExportFolder + "/atlas.png")
            if (!hashFile.exists() || !atlasFile.exists() || !atlasImageFile.exists()) {
                val pw = PrintWriter(hashFile)
                pw.print(-1)
                pw.close()
            }
            val s = Scanner(hashFile)
            c = s.nextInt()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        println("Finished; time elapsed: " + java.lang.Double.toString((System.currentTimeMillis() - initTime) / 1000.toDouble()) + "s")

        if (hashingTotal != c) {
            initTime = System.currentTimeMillis()
            println("Packing images...")

            var hadToDelete = false
            // delete images starting with _
            fun invalidFile(file: File) = (file.name.endsWith(".png") && file.name.startsWith("_"))

            for (file in File(origin).walk()) {
                if (file.isFile) {
                    if (invalidFile(file)) {
                        println("Deleting ${file.name}")
                        file.delete()
                        hadToDelete = true
                    }
                }
            }

            TexturePacker.process(origin, ExportFolder, "atlas")

            if (hadToDelete) hashingTotal = hashAllFiles(File(origin), hashFile)

            // save new hash value
            val pw = PrintWriter(hashFile)
            pw.print(hashingTotal)
            pw.close()

            println("Finished; time elapsed: " + java.lang.Double.toString((System.currentTimeMillis() - initTime) / 1000.toDouble()) + " s\n_____\n")
        }
    }
}