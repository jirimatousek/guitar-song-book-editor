/*
 *  Copyright (c) 2008 - Tomas Janecek.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package songer.exporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import songer.parser.nodes.ChordNode;
import songer.parser.nodes.LineNode;
import songer.parser.nodes.Node;
import songer.parser.nodes.SongBook;
import songer.parser.nodes.SongNode;
import songer.parser.nodes.TextNode;
import songer.parser.nodes.VerseNode;
import songer.util.FileIO;


public class LaTexExporter implements Exporter {
    private static Logger logger = LoggerFactory.getLogger(LaTexExporter.class);
    
    @Override
    public void export(File baseDir, SongBook songBook) {
        // Output
        File outputFile = new File(baseDir.getAbsoluteFile(),  "/tex/allsongs.tex" );
        logger.info("Starting export to latex file {}.", outputFile.getAbsolutePath());
        FileIO.createDirectoryIfRequired(outputFile);

        try {
            // Sort songs
            List<SongNode> sortedArrayList = new ArrayList<SongNode>(songBook.getSongNodeList());
            Collections.sort(sortedArrayList, new SongNode.TitleComparator());

            // Initialize exporter
            StringBuilder builder = new StringBuilder();

            // Build document
            for (SongNode songNode : songBook.getSongNodeList()) {
                // Build chapter
                appendSongNode(builder, songNode);
            }

            // Write to file
            FileIO.writeStringToFile(outputFile.getAbsolutePath(), "utf8", builder.toString());

            logger.info("COMPLETED export to latex file {}.", outputFile.getAbsolutePath());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to file " + outputFile.getName(), ex);
        }
    }

    private void appendSongNode(StringBuilder builder, SongNode songNode) {
        builder.append("\n\n\n\\begin{song}{").append(songNode.getTitle()).append("}\n");

        for (VerseNode verseNode : songNode.getVerseList()) {
            appendVerseNode(builder, verseNode);
            builder.append("\n\n");
        }
        builder.append("\\end{song}\n\n\n");

    }

    
    private void appendVerseNode(StringBuilder builder, VerseNode verseNode) {
        builder.append("\t\\begin{songverse}\n");

        for (Iterator<LineNode> it = verseNode.getLineNodes().iterator(); it.hasNext();) {
            LineNode lineNode = it.next();
            appendLineNode(builder, lineNode);
            builder.append((it.hasNext()) ? "\\\\ \n" : "\n");
        }

        builder.append("\t\\end{songverse}\n");
    }

    
    private void appendLineNode(StringBuilder builder, LineNode lineNode) {
        builder.append("\t\t");
        for (Node node : lineNode.getContentList()) {
            if (node instanceof  TextNode) {
                builder.append( ((TextNode) node).getText() );
            } else if (node instanceof  ChordNode) {
                ChordNode chordNode = (ChordNode) node;
                String chord2 = chordNode.getChord2(0).replaceAll("#", "\\\\#");
                String chord1 = chordNode.getChord1(0).replaceAll("#", "\\\\#");
                if (chord2.isEmpty()) {
                    builder.append("\\chord{").append(chord1).append("}");
                } else {
                    builder.append("\\chord{").append(chord2).append("/").append(chord1).append("}");
                }
            }
        }
    }
}