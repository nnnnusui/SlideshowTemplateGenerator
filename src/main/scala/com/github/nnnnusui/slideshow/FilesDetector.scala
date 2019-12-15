package com.github.nnnnusui.slideshow

import java.nio.file.{Files, Path}

import scalafx.scene.input.{DragEvent, Dragboard, TransferMode}

import scala.jdk.CollectionConverters._

object FilesDetector{
  def onDragOver(event: DragEvent): Unit ={
    if(!event.getDragboard.hasFiles) return
    event.acceptTransferModes(TransferMode.Link)
  }
  def onDragDropped(event: DragEvent, onDetect: Seq[Path]=> Unit): Unit ={
    val board = new Dragboard(event.getDragboard)
    if (!board.hasFiles) return
    onDetect(walkFiles(board))
    event.setDropCompleted(true)
  }
  private def walkFiles(dragboard: Dragboard): Seq[Path] ={
    dragboard.getFiles.asScala
      .map(_.toPath)
      .flatMap { it =>
        if (Files.isDirectory(it))
          Files.walk(it).filter(it => !Files.isDirectory(it))
            .iterator().asScala.toSeq
        else Seq(it)
      }.toSeq
  }
}
