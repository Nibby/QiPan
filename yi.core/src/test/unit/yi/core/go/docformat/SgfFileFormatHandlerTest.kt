package yi.core.go.docformat

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import yi.core.go.*
import yi.core.go.Annotation
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class SgfFileFormatHandlerTest {

    @Test
    fun `import linear sgf, works`() {
        val expectedMovePositions = HashMap<Int, Array<Int>>()
        expectedMovePositions[1] = arrayOf(3, 3)
        expectedMovePositions[2] = arrayOf(15, 3)
        expectedMovePositions[3] = arrayOf(3, 15)
        expectedMovePositions[4] = arrayOf(15, 16)
        expectedMovePositions[5] = arrayOf(15, 14)
        expectedMovePositions[6] = arrayOf(16, 14)
        expectedMovePositions[7] = arrayOf(16, 13)
        expectedMovePositions[8] = arrayOf(16, 15)
        expectedMovePositions[9] = arrayOf(15, 13)
        expectedMovePositions[10] = arrayOf(13, 16)
        expectedMovePositions[11] = arrayOf(15, 9)

        // method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/linear.sgf", FileFormat.SGF, this::class.java)

        // assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        // traverse down main variation
        var currentNode = root
        var moveNumber = 0

        while (currentNode.getChildNodeInMainBranch() != null) {
            currentNode = currentNode.getChildNodeInMainBranch()!!
            ++moveNumber

            Assertions.assertEquals(GameNodeType.MOVE_PLAYED, currentNode.getType(), "Each node in SGF is a primary move, but this node is parsed as something else.")

            val gameMove = currentNode.getPrimaryMove()!!
            val expectedMoveColor = if ((moveNumber) % 2 == 1) StoneColor.BLACK else StoneColor.WHITE
            Assertions.assertEquals(expectedMoveColor, gameMove.color, "Stone color mismatch at move ${currentNode.getMoveHistory().size}")

            val expectedPosition = expectedMovePositions[moveNumber]
            Assertions.assertArrayEquals(expectedPosition, arrayOf(gameMove.x, gameMove.y), "Stone position mismatch at move $moveNumber")

            if (!currentNode.isLastMoveInThisVariation()) {
                Assertions.assertTrue(currentNode.children.size == 1, "Linear SGF should only have 1 descendant at each node besides leaf")
            } else {
                Assertions.assertTrue(currentNode.children.size == 0, "Last child must not have a child")
            }
        }

        Assertions.assertEquals(12, currentNode.getMoveHistory().size, "Move history size mismatch")
    }

    @Test
    fun `import branched sgf, works`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/branches.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        Assertions.assertEquals(3, root.children.size)

        val rootChild1 = root.children[0]
        Assertions.assertTrue(rootChild1.isLastMoveInThisVariation())

        val rootChild3 = root.children[2]
        Assertions.assertTrue(rootChild3.isLastMoveInThisVariation())

        val rootChild2 = root.children[1]
        Assertions.assertEquals(1, rootChild2.children.size)

        val rootChild2a = rootChild2.children[0]
        Assertions.assertEquals(2, rootChild2a.children.size)

        val rootChild2b = rootChild2a.children[0]
        Assertions.assertEquals(1, rootChild2b.children.size)

        val rootChild2c = rootChild2b.children[0]
        Assertions.assertTrue(rootChild2c.isLastMoveInThisVariation())
    }

    @Test
    fun `import OGS-style SGF, works`() {
        /*
            OGS has a very peculiar format where a standard continuation is expressed as a
            new branch of size 1. So instead of:
                ;B[aa] ;W[ab] ;B[ac] ...
            We have:
                ;B[aa] (;W[ab] (;B[ac] (...)))
         */
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/ogsLinear.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()
        // traverse down main variation
        var currentNode = root
        var moveNumber = 0
        while (!currentNode.isLastMoveInThisVariation()) {
            currentNode = currentNode.getChildNodeInMainBranch()!!
            ++moveNumber
        }

        Assertions.assertEquals(188, moveNumber)
    }

    @Test
    fun `import SGF with stone edit, works`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/blink.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()

        val firstMove = root.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.STONE_EDIT, firstMove.getType())
        Assertions.assertEquals(StoneColor.WHITE, gameModel.getGameState(firstMove).boardPosition.getStoneColorAt(1, 1))

        val secondMove = firstMove.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.STONE_EDIT, secondMove.getType())
        Assertions.assertEquals(StoneColor.NONE, gameModel.getGameState(secondMove).boardPosition.getStoneColorAt(1, 1))

        val thirdMove = secondMove.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.STONE_EDIT, thirdMove.getType())
        Assertions.assertEquals(StoneColor.WHITE, gameModel.getGameState(thirdMove).boardPosition.getStoneColorAt(1, 1))
    }

    @Test
    fun `import SGF with two passes, works`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/twoPasses.sgf", FileFormat.SGF, this::class.java)

        // Assertions
        Assertions.assertEquals(19, gameModel.boardWidth)
        Assertions.assertEquals(19, gameModel.boardHeight)

        val root = gameModel.getRootNode()

        val firstMove = root.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.PASS, firstMove.getType())

        val secondMove = firstMove.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.PASS, secondMove.getType())

        Assertions.assertTrue(secondMove.isLastMoveInThisVariation())
    }

    @Test
    fun `import 1000 node SGF, is fast enough`() {
        val startTime = System.nanoTime()
        GameModelImporter.fromInternalResources("/sgf/1000_nodes.sgf", FileFormat.SGF, this::class.java)
        val finishTime = System.nanoTime()

        val maxDurationMs = 800L
        val duration = (finishTime - startTime)
        Assertions.assertTrue(duration < TimeUnit.MILLISECONDS.toNanos(maxDurationMs),
                "Too slow. Expected < $maxDurationMs, got $duration")
    }

    @Test
    fun `import SGF with tag values containing escaped delimiter symbols, parses correctly`() {
        // Method under test
        val gameModel = GameModelImporter.fromInternalResources("/sgf/escapedSymbols.sgf", FileFormat.SGF, this::class.java)

        val firstNode = gameModel.getRootNode().getChildNodeInMainBranch()!!
        Assertions.assertEquals("This comment has an escaped tag close ] character and meta-escaped characters \\\\]",
                firstNode.getComments(), "Escape symbols not parsed correctly")

        val customTagValue = firstNode.getMetadataSingleValue("CUSTOM_TAG")
        Assertions.assertEquals("] \\[ ]", customTagValue, "Custom tag value not correct")

        val secondNode = firstNode.getChildNodeInMainBranch()!!
        Assertions.assertTrue(secondNode.isLastMoveInThisVariation())
    }

    @Test
    fun `import corrupt SGF with tag value closure, ignores the tag and succeeds`() {
        val gameModel = GameModelImporter.fromInternalResources("/sgf/corruptTagData.sgf", FileFormat.SGF, this::class.java)
        val root = gameModel.getRootNode()

        val firstMove = root.getChildNodeInMainBranch()
        Assertions.assertNotNull(firstMove, "Unexpected end of sequence after root node")

        val secondMove = firstMove!!.getChildNodeInMainBranch()
        Assertions.assertNotNull(secondMove, "Unexpected end of sequence after first move")

        // Should expect no annotations because both annotation tags are conjoined, forming
        // one inconsistent value.
        Assertions.assertEquals(0, firstMove.getAnnotations().size)
    }

    @Test
    fun `import SGF with random whitespace between key delimiter, works`() {
        val gameModel = GameModelImporter.fromInternalResources("/sgf/spaceInTagData.sgf", FileFormat.SGF, this::class.java)

        val root = gameModel.getRootNode()
        Assertions.assertEquals("6.5", root.getMetadataSingleValue("KM"), "Komi value incorrect")
        Assertions.assertEquals(19, gameModel.boardWidth, "Board width size incorrect")
        Assertions.assertEquals(19, gameModel.boardHeight, "Board height size incorrect")
        Assertions.assertEquals("2020-08-23", root.getMetadataSingleValue("DT"), "Date value incorrect")

        val firstMove = root.getChildNodeInMainBranch()!!
        val annotations = firstMove.getAnnotations()
        Assertions.assertEquals(2, annotations.size)
        Assertions.assertTrue(annotations.contains(Annotation.Square(0, 0)))
        Assertions.assertTrue(annotations.contains(Annotation.Square(0, 1)))
        Assertions.assertEquals("test msg", firstMove.getComments())
    }

    @Test
    fun `essential SGF tags well covered by importer`() {
        // Check that the root node and game node are constructed correctly from
        // key SGF tags such as B, W, C, AB, AW etc.
        val gameModel = GameModelImporter.fromInternalResources("/sgf/standard.sgf", FileFormat.SGF, this::class.java)

        val rootNode = gameModel.getRootNode()
        val annotations = rootNode.getAnnotations()

        Assertions.assertEquals(GameNodeType.ROOT, rootNode.delta.type)
        Assertions.assertEquals(13, annotations.size)
        Assertions.assertTrue(annotations.contains(Annotation.Square(0, 0)), "Square missing at (0, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Triangle(0, 1)), "Triangle missing at (0, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Cross(1, 0)), "Cross missing at (1, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Circle(1, 1)), "Circle missing at (1, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(2, 0, "1")), "Label '1' missing at (2, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(3, 0, "2")), "Label '2' missing at (3, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(4, 0, "3")), "Label '3' missing at (4, 0)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(2, 1, "A")), "Label 'A' missing at (2, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(3, 1, "B")), "Label 'B' missing at (3, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Label(4, 1, "C")), "Label 'C' missing at (4, 1)")
        Assertions.assertTrue(annotations.contains(Annotation.Line(18, 0, 18, 3)), "Line missing (18, 0) -> (18, 3)")
        Assertions.assertTrue(annotations.contains(Annotation.Arrow(17, 0, 17, 3)), "Arrow missing (17, 0) -> (17, 3)")
        Assertions.assertTrue(annotations.contains(Annotation.Arrow(16, 3, 16, 0)), "Arrow missing (16, 3) -> (16, 0)")

        val move1 = rootNode.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move1.delta.type, "Move 1 node type incorrect")
        Assertions.assertEquals(Stone(3, 3, StoneColor.BLACK), move1.getPrimaryMove()!!, "First move stone position incorrect")
        Assertions.assertEquals(2, move1.getChildNodes().size, "First move continuation size incorrect")

        val move1a = move1.getChildNodesExcludingMainBranch()[0]
        Assertions.assertEquals(GameNodeType.STONE_EDIT, move1a.delta.type, "Move 1 variation node type incorrect")

        val move2 = move1.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move2.delta.type, "Move 2 node type incorrect")
        Assertions.assertEquals(Stone(15, 3, StoneColor.WHITE), move2.getPrimaryMove()!!, "Second move stone position incorrect")
        Assertions.assertEquals(2, move2.getChildNodes().size, "Second move continuation size incorrect")

        val move2a = move1.getChildNodesExcludingMainBranch()[0]
        Assertions.assertEquals(GameNodeType.STONE_EDIT, move2a.delta.type, "Move 2 variation node type incorrect")

        val move3 = move2.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move3.delta.type, "Move 3 node type incorrect")
        Assertions.assertEquals(Stone(3, 16, StoneColor.BLACK), move3.getPrimaryMove()!!, "Second move stone position incorrect")
        Assertions.assertEquals(1, move3.getChildNodes().size, "Third move continuation size incorrect")

        val move4 = move3.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.MOVE_PLAYED, move4.delta.type, "Move 4 node type incorrect")

        val move5 = move4.getChildNodeInMainBranch()!!
        Assertions.assertEquals(GameNodeType.PASS, move5.delta.type, "Move 5 node type incorrect")
    }

    @Test
    fun `import handicapped game has correct next move state`() {
        val model = GameModelImporter.fromInternalResources("/sgf/handicap.sgf", FileFormat.SGF, this.javaClass)
        Assertions.assertEquals(2, model.info.getHandicapCount(), "Incorrect handicap counter")

        val root = model.getRootNode()
        val handicaps = root.getStoneEdits()
        Assertions.assertTrue(handicaps.contains(Stone(15, 3, StoneColor.BLACK)), "Missing handicap stone at (15,3)")
        Assertions.assertTrue(handicaps.contains(Stone(3, 15, StoneColor.BLACK)), "Missing handicap stone at (3, 15)")

        val playedMove = model.editor.addMove(0, 0)
        Assertions.assertEquals(StoneColor.WHITE, playedMove.moveNode!!.getPrimaryMove()!!.color,
                "Next move color incorrect")
    }

    @Test
    fun `import handicapped game from OGS has correct next move state`() {
        val model = GameModelImporter.fromInternalResources("/sgf/ogsHandicap.sgf", FileFormat.SGF, this.javaClass)
        Assertions.assertEquals(1, model.info.getHandicapCount(), "Incorrect handicap counter")

        val root = model.getRootNode()
        val handicaps = root.getStoneEdits()
        Assertions.assertTrue(handicaps.contains(Stone(3, 15, StoneColor.BLACK)), "Missing handicap stone at (3, 15)")

        val playedMove = model.editor.addMove(0, 0)
        Assertions.assertEquals(StoneColor.WHITE, playedMove.moveNode!!.getPrimaryMove()!!.color,
            "Next move color incorrect")
    }

    @Test
    fun `export empty model works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.info.setKomi(5.5f)

        val output = ByteArrayOutputStream()
        GameModelExporter.toOutputStream(model, output, FileFormat.SGF)
        val data = output.toString(Charsets.UTF_8)

        Assertions.assertTrue(data.contains("GM[1]"), "Game mode not correct. Exported: $data")
        Assertions.assertTrue(data.contains("FF[" + SgfFileFormatHandler.SgfExporter.SGF_EXPORTED_FILE_FORMAT_VERSION + "]"), "File format version mismatch. Exported: $data")
        Assertions.assertTrue(data.contains("SZ[19]"), "Board size not correct. Exported: $data")
        Assertions.assertTrue(data.contains("KM[5.5]"), "Komi value not correct. Exported: $data")
        Assertions.assertTrue(data.contains("RU[" + StandardGameRules.CHINESE.rulesHandler.getInternalName() + "]"), "Ruleset name mismatch. Exported: $data")

        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export linear model works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 0)
                .playMove(2, 0)
                .playMove(3, 0)

        val data = exportModel(model)
        
        Assertions.assertEquals(5, getNodeStartCount(data), "Node start count incorrect. Exported: $data") // 4 move nodes + 1 root node

        val segments = data.split(SgfFileFormatHandler.DELIM_NODE_START)
        // Expect 6 because of leading '('
        // This method of segmenting node data is a little dirty, but it's the quickest method.
        Assertions.assertEquals(6, segments.size, "Data segment size incorrect. Exported: $data")

        val move1 = segments[2]
        val move2 = segments[3]
        val move3 = segments[4]
        val move4 = segments[5]

        Assertions.assertTrue(move1 == "B[aa]", "Move 1 data incorrect. Exported: $data")
        Assertions.assertTrue(move2 == "W[ba]", "Move 2 data incorrect. Exported: $data")
        Assertions.assertTrue(move3 == "B[ca]", "Move 3 data incorrect. Exported: $data")
        Assertions.assertTrue(move4 == "W[da])", "Move 4 data incorrect. Exported: $data")

        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export branched model works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.beginMoveSequence()
                .playMove(0, 0)
                .playMove(1, 1)
                .pass()

        model.currentNode = model.getRootNode()
        model.beginMoveSequence()
                .pass()

        model.currentNode = model.getRootNode()
        model.beginMoveSequence()
                .playMove(1, 1)
                .pass()

        val move1 = model.getRootNode().getChildNodeInMainBranch()!!
        model.currentNode = move1
        model.beginMoveSequence()
                .pass()

        /*
            At this point we should have a tree that looks like this:
            [Root] ------- | --- |
              |            |     |
             [1] --- |   [PASS] [1]
              |      |           |
             [2]   [PASS]      [PASS]
              |
            [PASS]
         */

        val data = exportModel(model).replace(" ", "").replace("\n", "")
        // Regardless of what the root data may contain, this part should be identical
        Assertions.assertTrue(data.endsWith("(;B[aa](;W[bb];B[])(;W[]))(;B[])(;B[bb];W[]))"),
                "Node data incorrect after root node (that is not to say root is correct, it is untested). " +
                        "Exported: $data")
        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export multi-value tag works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        val root = model.getRootNode()
        model.editor.addStoneEdit(root, Stone(0, 0, StoneColor.BLACK))
        model.editor.addStoneEdit(root, Stone(1, 0, StoneColor.BLACK))

        model.editor.addStoneEdit(root, Stone(2, 0, StoneColor.WHITE))
        model.editor.addStoneEdit(root, Stone(3, 0, StoneColor.WHITE))

        model.editor.addAnnotations(root, setOf(Annotation.Triangle(0, 0), Annotation.Triangle(1, 0)))

        val data = exportModel(model)

        assertContainsVariant(setOf("AB[aa][ba]", "AB[ba][aa]"), data, "Black stone edits exported incorrectly. Exported=$data")
        assertContainsVariant(setOf("AW[ca][da]", "AW[da][ca]"), data, "White stone edits exported incorrectly. Exported=$data")
        assertContainsVariant(setOf("TR[aa][ba]", "TR[ba][aa]"), data, "Annotations exported incorrectly. Exported=$data")
        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export directional annotation works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        val root = model.getRootNode()
        model.editor.addAnnotations(root, listOf(Annotation.Arrow(0, 0, 2, 2), Annotation.Arrow(1, 1, 3, 3)))
        model.editor.addAnnotations(root, listOf(Annotation.Line(1, 1, 4, 4), Annotation.Line(3, 3, 5, 5)))

        val data = exportModel(model)
        assertContainsVariant(setOf("AR[aa:cc][bb:dd]", "AR[bb:dd][aa:cc]"), data, "Arrow annotations not exported correctly. Exported=$data")
        assertContainsVariant(setOf("LN[bb:ee][dd:ff]", "LN[dd:ff][bb:ee]"), data, "Line annotations not exported correctly. Exported=$data")
        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export non-square board size works`() {
        val model = GameModel(19, 8, StandardGameRules.CHINESE)
        val data = exportModel(model)

        Assertions.assertTrue(data.contains("SZ[19:8]"), "Board size value not correct. Exported=$data")
        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export handicapped game works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.info.setHandicapCount(2)
        model.editor.addStoneEdit(model.getRootNode(), Stone(3, 3, StoneColor.BLACK))
        model.editor.addStoneEdit(model.getRootNode(), Stone(15, 15, StoneColor.BLACK))

        val data = exportModel(model)

        Assertions.assertTrue(data.contains("HA[2]"), "Handicap counter not correct. Exported=$data")
        assertContainsVariant(setOf("AB[dd][pp]", "AB[pp][dd]"), data, "Handicap stone position not correct. Exported=$data")

        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export passes works`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.beginMoveSequence()
                .playMove(1, 1)
                .pass()
                .pass()

        val data = exportModel(model)
        Assertions.assertEquals(4, getNodeStartCount(data), "Node start count incorrect. Exported: $data")

        val segments = data.split(SgfFileFormatHandler.DELIM_NODE_START)
        val move1 = segments[2]
        val move2 = segments[3]
        val move3 = segments[4]

        Assertions.assertTrue(move1 == "B[bb]", "Move 1 data incorrect. Exported: $data")
        Assertions.assertTrue(move2 == "W[]", "Move 2 data incorrect. Exported: $data")
        Assertions.assertTrue(move3 == "B[])", "Move 3 data incorrect. Exported: $data")

        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export all annotations correctly`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.editor.addAnnotationToCurrentNode(Annotation.Triangle(0, 0))
        model.editor.addAnnotationToCurrentNode(Annotation.Square(1, 0))
        model.editor.addAnnotationToCurrentNode(Annotation.Circle(2, 0))
        model.editor.addAnnotationToCurrentNode(Annotation.Cross(3, 0))
        model.editor.addAnnotationToCurrentNode(Annotation.Fade(4, 0))
        model.editor.addAnnotationToCurrentNode(Annotation.Label(5, 0, "a"))
        model.editor.addAnnotationToCurrentNode(Annotation.Label(6, 0, "1"))
        model.editor.addAnnotationToCurrentNode(Annotation.Line(1, 1, 2, 2))
        model.editor.addAnnotationToCurrentNode(Annotation.Arrow(3, 3, 4, 4))

        val data = exportModel(model)

        Assertions.assertTrue(data.contains("TR[aa]"), "Triangle annotation not correct. Exported: $data")
        Assertions.assertTrue(data.contains("SQ[ba]"), "Square annotation not correct. Exported: $data")
        Assertions.assertTrue(data.contains("CR[ca]"), "Circle annotation not correct. Exported: $data")
        Assertions.assertTrue(data.contains("MA[da]"), "Cross annotation not correct. Exported: $data")
        Assertions.assertTrue(data.contains("DD[ea]"), "Fade annotation not correct. Exported: $data")
        assertContainsVariant(setOf("LB[fa:a][ga:1]", "LB[ga:1][fa:a]"), data, "Label annotations not correct. Exported: $data")
        Assertions.assertTrue(data.contains("LN[bb:cc]"), "Line annotation not correct. Exported: $data")
        Assertions.assertTrue(data.contains("AR[dd:ee]"), "Arrow annotation not correct. Exported: $data")

        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `export all stone edits correctly`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.beginMoveSequence().playMove(9, 9)
        val move1 = model.getRootNode().getChildNodeInMainBranch()!!
        model.editor.addStoneEdit(move1, Stone(0, 0, StoneColor.BLACK))
        model.editor.addStoneEdit(move1, Stone(1, 0, StoneColor.WHITE))
        model.editor.addStoneEdit(move1, Stone(9, 9, StoneColor.NONE))

        val data = exportModel(model)
        val segments = data.split(SgfFileFormatHandler.DELIM_NODE_START)
        val move1Data = segments[2]

        Assertions.assertTrue(move1Data.contains("AE[jj]"), "Stone erasure not correct. Exported: $data")
        Assertions.assertTrue(move1Data.contains("AB[aa]"), "Added black stone not correct. Exported: $data")
        Assertions.assertTrue(move1Data.contains("AW[ba]"), "Added white stone not correct. Exported: $data")

        testExportedFormatCorrectness(data, model)
    }

    @Test
    fun `exports node metadata correctly`() {
        val model = GameModel(19, 19, StandardGameRules.CHINESE)
        model.beginMoveSequence().pass()

        val root = model.getRootNode()
        root.putMetadata("CUSTOM_KEY", "some value")
        root.putMetadata("CUSTOM_KEY2", "")

        val child = root.getChildNodeInMainBranch()!!
        child.putMetadata("CUSTOM", "child value")

        val data = exportModel(model)
        val segments = data.split(SgfFileFormatHandler.DELIM_NODE_START)
        Assertions.assertEquals(3, segments.size, "Exported data segment size mismatch. Exported: $data")

        val rootData = segments[1]
        Assertions.assertTrue(rootData.contains("CUSTOM_KEY[some value]"), "CUSTOM_KEY not exported")
        Assertions.assertTrue(rootData.contains("CUSTOM_KEY2[]"), "CUSTOM_KEY2 (with empty value) not exported")

        val childData = segments[2]
        Assertions.assertTrue(childData.contains("CUSTOM[child value]"), "CUSTOM not exported")
    }

    @Test
    fun `imports node metadata correctly`() {
        val data = "(;GM[1]FF[4]SZ[19]CUSTOM_METADATA[abc123]MULTI[a][b])"
        val model = GameModelImporter.fromString(data, FileFormat.SGF)
        val root = model.getRootNode()

        Assertions.assertTrue(root.getMetadataKeys().contains("CUSTOM_METADATA"), "Custom metadata key not imported")
        Assertions.assertEquals("abc123", root.getMetadataSingleValue("CUSTOM_METADATA"))

        Assertions.assertTrue(root.getMetadataKeys().contains("MULTI"), "Custom metadata key not imported")
        val multiValues = root.getMetadataMultiValue("MULTI")
        when {
            multiValues[0] == "a" -> {
                Assertions.assertEquals("b", multiValues[1], "Multi-value metadata not imported correctly")
            }
            multiValues[0] == "b" -> {
                Assertions.assertEquals("a", multiValues[1], "Multi-value metadata not imported correctly")
            }
            else -> {
                Assertions.fail<String>("Multi-value metadata not imported correctly")
            }
        }
    }

    @Test
    fun `treats out of bounds move coordinates as pass`() {
        /*
            Some editors such as Lizzie uses [tt] (19,19) to represent a pass...
         */
        val data = "(;GM[1]FF[4]SZ[19];B[aa];W[tt])"
        val model = GameModelImporter.fromString(data, FileFormat.SGF)
        val root = model.getRootNode()
        val firstChild = root.getChildNodeInMainBranch()!!
        val secondChild = firstChild.getChildNodeInMainBranch()!!

        Assertions.assertEquals(GameNodeType.PASS, secondChild.getType())
    }

    @Test
    fun `treats branch end delimiter in tag value correctly`() {
        val data = "(;GM[1]FF[4]SZ[19];B[aa];C[:-);\\[internal value :)\\]])"
        val model = GameModelImporter.fromString(data, FileFormat.SGF)
        val root = model.getRootNode()
        val firstChild = root.getChildNodeInMainBranch()!!
        val secondChild = firstChild.getChildNodeInMainBranch()!!

        Assertions.assertEquals(":-);\\[internal value :)]", secondChild.getComments())
    }

    @Test
    fun `import from IGS file format works`() {
        val game = GameModelImporter.fromInternalResources("/sgf/igsGame.sgf", FileFormat.SGF, this::class.java)
        // TODO: Make some assertions
    }

    private fun exportModel(gameModel: GameModel): String {
        val output = ByteArrayOutputStream()
        GameModelExporter.toOutputStream(gameModel, output, FileFormat.SGF)
        return output.toString(Charsets.UTF_8)
    }

    /*
     * Asserts the value to be tested contains at least one of the strings in the variant set.
     * Fails the test case if this is not true.
     *
     * Note: This method is introduced because the annotation and stone edit order is
     * non-deterministic due to the use of Set rather than List. We want to retain the
     * unique-value property of Sets in annotation/stone edits.
     */
    private fun assertContainsVariant(variants: Set<String>, toTest: String, failureMessage: String) {
        var hasVariant = false
        for (variant in variants) {
            if (toTest.contains(variant)) {
                hasVariant = true
                break
            }
        }

        Assertions.assertTrue(hasVariant, failureMessage)
    }

    private fun getNodeStartCount(data: String): Int {
        var nodeStartCount = 0
        for (charCode in data.chars()) {
            if (charCode.toChar() == SgfFileFormatHandler.DELIM_NODE_START) {
                ++nodeStartCount
            }
        }
        return nodeStartCount
    }

    /*
        Checks that the exported data is well formed -- that is, every opening delimiter
        has a matching closing one.
     */
    private fun testExportedFormatCorrectness(data: String, gameModel: GameModel) {
        var branchStarts = 0
        var branchEnds = 0
        var nodeCounts = 0
        var valueStarts = 0
        var valueEnds = 0
        var prevChar: Char? = null

        for (charCode in data.chars()) {
            val char = charCode.toChar()

            when (char) {
                SgfFileFormatHandler.DELIM_NODE_START -> ++nodeCounts
                SgfFileFormatHandler.DELIM_BRANCH_START -> ++branchStarts
                SgfFileFormatHandler.DELIM_BRANCH_END -> ++branchEnds
                SgfFileFormatHandler.DELIM_TAG_VALUE_START -> ++valueStarts
                SgfFileFormatHandler.DELIM_TAG_VALUE_END -> {
                    if (prevChar != '\\') {
                        ++valueEnds
                    }
                }
            }

            prevChar = char
        }

        Assertions.assertEquals(branchStarts, branchEnds, "Start/end delimiter count mismatch for branches. Exported: $data")
        Assertions.assertEquals(valueStarts, valueEnds, "Start/end delimiter count mismatch for tag values. Exported: $data")
        Assertions.assertEquals(countNodes(gameModel.getRootNode()), nodeCounts, "Node count incorrect after export. Exported: $data")
    }

    private fun countNodes(node: GameNode): Int {
        var nodeCount = 0
        var current = node
        do {
            ++nodeCount
            for (branchNode in current.getChildNodesExcludingMainBranch()) {
                nodeCount += countNodes(branchNode)
            }
            if (current.isLastMoveInThisVariation()) {
                break
            } else {
                current = current.getChildNodeInMainBranch()!!
            }
        } while (true)

        return nodeCount
    }
}