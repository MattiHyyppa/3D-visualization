package raycasting

import java.awt.image.BufferedImage
import java.io.{File, IOException}
import javax.imageio.ImageIO

/**
 * This class is used for creating different textures on the walls.
 * 
 * @constructor creates a new Texture object
 * @param location is the texture image's path
 * @param size the texture image contains (size * size) pixels
 */
class Texture(location: String, val size: Int = 64) {
  var pixels: Array[Int] = Array.ofDim[Int](size * size)
  load()
  
  // Load the image from file
  private def load() = {
    try {
      val image: BufferedImage = ImageIO.read(new File(location))
      val w = image.getWidth
      val h = image.getHeight
      image.getRGB(0, 0, w, h, pixels, 0, w)
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }
}

/**
 * The companion object Texture makes it easier to use the textures.
 */
object Texture {
  val colorstone = new Texture("pics/colorstone.png")
  val greystone = new Texture("pics/greystone.png")
  val redBrick = new Texture("pics/redbrick.png")
}  