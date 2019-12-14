package com.github.nnnnusui.slideshow

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.BorderPane
import scalafx.stage.FileChooser

object SlideshowBuilder extends JFXApp{
  val timeline = new Timeline
  private val saveButton = new Button("to .exo"){ onAction = _=> saveToExo() }
  private val label = Label("")
  timeline.value.onChange{ (_, _)=> label.text = timeline.value.size.toString }

  stage = new PrimaryStage{
    title = "Slideshow Builder"
    scene = new Scene{
      root = new BorderPane{
        center = timeline.view
        bottom = new BorderPane{
          center = label
          right = saveButton
        }
      }
    }
  }

  def saveToExo(): Unit ={
    val chooser = new FileChooser()
    chooser.setInitialDirectory(Paths.get(".").toFile)
    val result = chooser.showSaveDialog( stage )
    if (result == null) return
    if (timeline.value.isEmpty) return

    val header = Exo.Header(1280, 720, 30)
    val objects = timeline.value.map(it=> Exo.Object.MediaObject.Picture(it.path.toString)).toSeq
    val timelineObjects = TemplateGenerator.generate(128, 30, 1, objects)
    val exo = Exo(header, timelineObjects)
    Files.writeString(result.toPath, exo.toExo, Charset.forName("Shift_JIS"))
  }
}
