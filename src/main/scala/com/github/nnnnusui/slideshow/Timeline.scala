package com.github.nnnnusui.slideshow

import java.nio.file.{Files, Path}

import javafx.scene.{input => jfxsi}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListCell, ListView}
import scalafx.scene.input._

class Timeline {
  //  val timelineLog = List[ObservableBuffer[Picture]]() // TODO: [Undo, Redo] for timeline
  val value = new ObservableBuffer[Picture]
  val view: ListView[Picture] = new ListView[Picture]{
    items.set(value)
    //    selectionModel //TODO: support Multiple selection
    cellFactory = _=> new TimelineCell
    onDragOver    = event=>{ FilesDetector.onDragOver(new DragEvent(event));    event.consume() }
    onDragDropped = event=>{ FilesDetector.onDragDropped(new DragEvent(event)); event.consume() }
  }

  object FilesDetector{
    def onDragOver(event: DragEvent): Unit ={
      if(!event.getDragboard.hasFiles) return
      event.acceptTransferModes(TransferMode.Link)
    }
    def onDragDropped(event: DragEvent, index: Int = 0): Unit ={
      import scala.jdk.CollectionConverters._
      val board = new Dragboard(event.getDragboard)
      if (!board.hasFiles) return
      val pictures = walkFiles(board).map(it=> Picture(it.toAbsolutePath))
      value.addAll(index, pictures.asJava)
      event.setDropCompleted(true)
    }
    private def walkFiles(dragboard: Dragboard): Seq[Path] ={
      import scala.jdk.CollectionConverters._
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

  class TimelineCell extends ListCell[Picture] {
    item.onChange{ (_, _, value)=> text = if (value== null) "" else value.toString }
    onMouseClicked = event=> if (event.getButton == jfxsi.MouseButton.MIDDLE) remove()
    onDragDetected = event=>{
      LocalSorting.onDetect(new MouseEvent(event))
      event.consume()
    }
    onDragOver = event=>{
      LocalSorting.onDragOver(new DragEvent(event))
      FilesDetector.onDragOver(new DragEvent(event))
      event.consume()
    }
    onDragEntered = _=> listView.get().getSelectionModel.select(index.value)
    onDragDropped = event=>{
      LocalSorting.onDragDropped(new DragEvent(event))
      FilesDetector.onDragDropped(new DragEvent(event), Seq(value.size, index.value).min)
      event.consume()
    }
    def remove(): Unit = {
      if(!empty.value)
        value.remove(index.value)
    }
    object LocalSorting{
      def onDetect(event: MouseEvent): Unit ={
        if (item == null) return
        val board = startDragAndDrop(TransferMode.Move)
        val content = new ClipboardContent()
        content.putString(index.value.toString)
        board.setContent(content)
      }
      def onDragOver(event: DragEvent): Unit ={
        if(!event.getDragboard.hasString) return
        event.acceptTransferModes(TransferMode.Move)
      }
      def onDragDropped(event: DragEvent): Unit ={
        val board = event.getDragboard
        if (!board.hasString) return
        val sourceIndex = board.getString.toInt
        val source = value(sourceIndex)
        value.remove(sourceIndex)
        val targetIndex = Seq(index.value, value.size).min
        value.add(targetIndex, source)
        event.setDropCompleted(true)
      }
    }
  }
}
