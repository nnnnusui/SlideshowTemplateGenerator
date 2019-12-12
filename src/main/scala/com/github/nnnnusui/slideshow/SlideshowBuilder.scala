package com.github.nnnnusui.slideshow

import java.nio.file.{Files, Path, Paths}

import com.github.nnnnusui.slideshow.Exo.Object.MediaObject
import javafx.scene.input.Dragboard
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ListCell, ListView, ScrollPane}
import scalafx.scene.input.{ClipboardContent, TransferMode}
import scalafx.scene.layout.BorderPane
import scalafx.stage.FileChooser

object SlideshowBuilder extends JFXApp{
  val timeline = new ObservableBuffer[Picture]
  private val timelineView: ListView[Picture] = new ListView[Picture]{
    items.set(timeline)
    cellFactory = _=> new TimelineCell
    onDragOver = event=>{
      if(event.getDragboard.hasFiles)
        event.acceptTransferModes(TransferMode.Link)
      event.consume()
    }
    onDragDropped = event=>{
      val dragboard = event.getDragboard
      if(dragboard.hasFiles) {
        walkFiles(dragboard).map(it=> Picture(it.toAbsolutePath))
          .foreach(it=> timeline.add(it))
        event.setDropCompleted(true)
      } else {
        event.setDropCompleted(false)
      }
      event.consume()
    }
  }
  stage = new PrimaryStage{
    title = "Slideshow Builder"
    scene = new Scene{
      root = new BorderPane{
        center = timelineView
        bottom = new Button("to .exo"){
          onAction = _=> saveToExo()
        }
      }
    }
  }

  def saveToExo(): Unit ={
    val chooser = new FileChooser()
    chooser.setInitialDirectory(Paths.get(".").toFile)
    val result = chooser.showSaveDialog( stage )
    if (result == null) return
    if (timeline.isEmpty) return

    val header = Exo.Header(1280, 720, 30)
    val objects = timeline.map(it=> Exo.Object.MediaObject.Picture(it.path.toString)).toSeq
    val timelineObjects = TemplateGenerator.generate(128, 30, 1, objects)
    val exo = Exo(header, timelineObjects)
    Files.writeString(result.toPath, exo.toExo)
  }

  def walkFiles(dragboard: Dragboard): Seq[Path] ={
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


  class TimelineCell extends ListCell[Picture] {
    item.onChange{ (_, _, value)=> text = if (value== null) "" else value.toString }
    onDragDetected = event=>{
      if (item != null){
        val board = startDragAndDrop(TransferMode.Move)
        val content = new ClipboardContent()
        content.putString(index.value.toString)
        board.setContent(content)
        event.consume()
      }
    }
    onDragOver = event=>{
      event.getDragboard match {
        case it if it.hasString => event.acceptTransferModes(TransferMode.Move)
        case it if it.hasFiles => event.acceptTransferModes(TransferMode.Link)
      }
      event.consume()
    }
    onDragDropped = event=>{
      event.getDragboard match {
        case it if it.hasFiles =>
          walkFiles(it).map(it=> Picture(it.toAbsolutePath))
            .foreach(it=> timeline.add(it))
          event.setDropCompleted(true)
        case it if it.hasString =>
          val sourceIndex = it.getString.toInt
          val source = timeline(sourceIndex)
          timeline.remove(sourceIndex)
          val targetIndex = Seq(index.value, timeline.size).min
          timeline.add(targetIndex, source)
          event.setDropCompleted(true)
      }
      event.consume()
    }
  }
}

case class Picture(path: Path){
  override def toString: String = path.getFileName.toString
}