package raycasting

import scala.io.Source
import scala.collection.mutable.Buffer
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io._
import java.awt._
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.event.{ KeyListener, KeyEvent }
import java.awt.image.BufferStrategy
import javax.swing.JFrame
import javax.swing.WindowConstants

/**
 * An instance of this class will take care of rendering the image on the window. In addition, this class reads the world map from Map.txt file.
 * The calculations needed to draw the walls are carried out in the Screen class.
 */

class Renderer extends Runnable {

  // Width and height of the window
  val preferredWidth = 1200
  val preferredHeight = preferredWidth / 12 * 9

  // The image to be drawn on the window
  val image = new BufferedImage(preferredWidth, preferredHeight, BufferedImage.TYPE_INT_RGB)
  // The pixels of the image
  val pixels = image.getRaster().getDataBuffer().asInstanceOf[DataBufferInt].getData()
  val thread = new Thread(this)
  private var running = false

  // World map
  val map = this.getMap
  // The player and screen
  val player = new Player
  val screen = new Screen(Array(Texture.colorstone, Texture.greystone, Texture.redBrick), map, preferredWidth, preferredHeight)

  val frame: JFrame = new JFrame("3D visualization")
  frame.setPreferredSize(new Dimension(preferredWidth, preferredHeight))
  frame.setMaximumSize(new Dimension(preferredWidth, preferredHeight))
  frame.setMinimumSize(new Dimension(preferredWidth, preferredHeight))

  frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  frame.setResizable(false)
  frame.setLocationRelativeTo(null)
  frame.setTitle("3D visualization")
  frame.setBackground(Color.black)
  frame.addKeyListener(screen)
  frame.setVisible(true)
  start()

  private def start() = synchronized {
    running = true
    thread.start()
  }

  def stop() = synchronized {
    running = false
    try {
      thread.join()
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }

  def render(): Unit = {
    val bs: BufferStrategy = frame.getBufferStrategy
    if (bs == null) {
      frame.createBufferStrategy(3)
      return
    }
    val g: Graphics = bs.getDrawGraphics
    g.drawImage(image, 0, 0, image.getWidth, image.getHeight, null)
    bs.show()
  }

  def run = {
    var lastTime = System.nanoTime()
    val ns = 1000000000.0 / 60.0
    var delta = 0.0
    frame.requestFocus()
    while (running) {
      val now = System.nanoTime()
      delta = delta + ((now - lastTime) / ns)
      lastTime = now
      // Update screen about 60 times per second.
      while (delta >= 1) {
        this.screen.update(player, pixels)
        this.screen.updatePlayer(player)
        delta = delta - 1
      }
      render()
    }
  }

  /** Read the world map from a file and store it in a 2D array.*/
  def getMap = {
    val mapBuffer: Buffer[Array[Int]] = Buffer()

    // Read map
    val bufferedSource = Source.fromFile("Map.txt")
    try {
      val lines = bufferedSource.getLines()
      for (line <- lines) {
        val rowOfStrings = line.split("")
        val row = rowOfStrings.map(x => x.toInt)
        mapBuffer += row
      }

    } catch {
      case e: IOException => println("Something went wrong while reading the Map.txt file.")
    } finally {
      bufferedSource.close()
    }

    mapBuffer.toArray
  }

}