package raycasting

import scala.math._
import java.awt.Color
import java.awt.event.{ KeyListener, KeyEvent }

/* Original source of the algorithm for calculating the ray length and line height:
 * https://lodev.org/cgtutor/raycasting.html
 */

/**
 * The Screen class will take care of the calculations needed for the ray casting algorithm. The calculations needed for rotating
 * and moving in the room will also be taken care of in this class.
 *
 *  @constructor creates a new screen
 *  @param textures are used to make fancier walls
 *  @param map is the world map
 *  @param width is the width of the window
 *  @param height is the height of the window
 *
 */
class Screen(val textures: Array[Texture], map: Array[Array[Int]], width: Int, height: Int) extends KeyListener {

  // Colors for the wall and the ceiling.
  val ceilingColor = new Color(60, 60, 60)
  val floorColor = new Color(120, 120, 120)

  /**
   * Calculates the distance from the player's position to the nearest wall in the ray direction. The wall height is determined based on the 
   * distance. In addition, this method adds textures to the walls based on the wall value used in the map (0 for floor, 1..3 for different 
   * wall textures).
   *
   *  @param player represents the person in the room
   *  @param pixels contain the rgb values for the image to be drawn on the window
   *  @return an array of the modified pixels
   */

  def update(player: Player, pixels: Array[Int]): Array[Int] = {

    // Clear the screen. Draw the ceiling and floor by setting the first half of the window pixels to the ceiling color and the rest of the pixels 
    // to the floor color.
    var i = 0
    while (i < pixels.size / 2) {
      if (pixels(i) != ceilingColor.getRGB) {
        pixels(i) = ceilingColor.getRGB
      }
      i += 1
    }
    while (i < pixels.size) {
      if (pixels(i) != floorColor.getRGB) {
        pixels(i) = floorColor.getRGB
      }
      i += 1
    }

    // Go through horizontal pizels.
    for (x <- 0 until width) {
      /* t is a coefficient that gets the value -1 on the left side of the window, 0 at the center and 1 at the right side
       * of the window. Using t as a coefficient of planeX and planeY, the loop goes through every ray in the field of view.
       */
      val t = 2 * x / width.toDouble - 1

      // Values for the ray vector's x- and y-components.
      val rayDirX = player.dirX + t * player.planeX
      val rayDirY = player.dirY + t * player.planeY

      // The ray is sent from the player's position.
      var posX = player.posX
      var posY = player.posY

      // The square in the world map in which the ray is at the moment.
      var mapX = posX.toInt
      var mapY = posY.toInt

      // The distance from the x-side of current square to the next x-side.
      val dx = abs(1 / rayDirX)
      // The distance from the y-side of the current square to the next y-side.
      val dy = abs(1 / rayDirY)
      // Contains the distance to the first wall that the ray hits.
      var distToWall = 0.0
      /* If rayDirX > 0, stepX will be set to 1. Otherwise, stepX will be set to -1. Similarly for stepY. stepX and
       * stepY will be needed for determining the next square in the ray direction.
       */
      var stepX = 0
      var stepY = 0

      // wallHit == 0 as long as no wall was hit. Otherwise, wallHit is set to 1.
      var wallHit = 0
      // wallSide == 0 if x-side of a square was hit. Otherwise, wallSide == 1.
      var wallSide = 0

      /* xDist will be the distance to the first x-side and yDist will be the distance to the first y-side. After that, xDist will
       * be the distance from start to the next x-side. Similarly to yDist.
       */
      var xDist = 0.0
      var yDist = 0.0

      // Open the picture (demo_pic.PNG) in the root folder to see the explanation for the following if block.
      if (rayDirX < 0) {
        stepX = -1
        xDist = (posX - mapX) * dx
      } else {
        stepX = 1
        xDist = (mapX + 1.0 - posX) * dx
      }

      if (rayDirY < 0) {
        stepY = -1
        yDist = (posY - mapY) * dy
      } else {
        stepY = 1
        yDist = (mapY + 1.0 - posY) * dy
      }

      // Continue loop as long as no wall has been hit.
      while (wallHit == 0) {
        // If the next x-side of a square is closer than the next y-side of a square, move in x direction in the map.
        if (xDist < yDist) {
          xDist += dx
          mapX += stepX
          wallSide = 0
        } else {
          yDist += dy
          mapY += stepY
          wallSide = 1
        }
        // Check if a wall was hit.
        if (map(mapY)(mapX) > 0) wallHit = 1
      }
      
      if (wallSide == 0) {
        distToWall = abs((mapX - posX + (1 - stepX) / 2) / rayDirX)
      } else {
        distToWall = abs((mapY - posY + (1 - stepY) / 2) / rayDirY)
      }

      val lineHeight = (if (distToWall > 0) height / distToWall else height).toInt
      val start = max(0, -lineHeight / 2 + height / 2)
      val end = min(height - 1, lineHeight / 2 + height / 2)

      val textureNum = map(mapY)(mapX) - 1

      // Exact position of where the wall was hit
      var wallX = 0.0
      if (wallSide == 1) {
        wallX = posX + distToWall * rayDirX
      } else {
        wallX = posY + distToWall * rayDirY
      }

      wallX -= floor(wallX)
      // x coordinate on the texture
      var texX = (wallX * textures(textureNum).size).toInt
      if ((wallSide == 0 && rayDirX > 0) || (wallSide == 1 && rayDirY < 0)) texX = textures(textureNum).size - texX - 1

      // calculate y coordinate on the texture
      for (y <- start until end) {
        val texY = (((y * 2 - height + lineHeight) << 6) / lineHeight) / 2
        var color = textures(textureNum).pixels(min(max(texX + (texY * textures(textureNum).size), 0), pow(textures(textureNum).size, 2).toInt - 1))

        // Make y sides darker.
        if (wallSide == 1) color = (color >> 1) & 8355711
        pixels(x + y * width) = color
      }

    }
    pixels
  }

  // Variables to keep track of if the player is moving or rotating.
  private var left, right, forward, back = false
  // Speed for rotating and moving.
  val rotateSpeed = 0.045
  val moveSpeed = 0.082

  def keyPressed(e: KeyEvent) = {
    if (e.getKeyCode == KeyEvent.VK_LEFT) {
      left = true
    }
    if (e.getKeyCode == KeyEvent.VK_RIGHT) {
      right = true
    }
    if (e.getKeyCode == KeyEvent.VK_UP) {
      forward = true
    }
    if (e.getKeyCode == KeyEvent.VK_DOWN) {
      back = true
    }
  }

  def keyReleased(e: KeyEvent) = {
    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
      left = false
    }
    if (e.getKeyCode == KeyEvent.VK_RIGHT) {
      right = false
    }
    if (e.getKeyCode == KeyEvent.VK_UP) {
      forward = false
    }
    if (e.getKeyCode == KeyEvent.VK_DOWN) {
      back = false
    }
  }

  def keyTyped(e: KeyEvent) = {
    // This method does nothing, but it has to be defined because Screen class implements KeyListener.
  }

  /**
   * Update the player's position if up or down arrows are pressed. Rotate the player if left or right arrows are pressed.
   * 
   * @param player is the person in the room
   */
  def updatePlayer(player: Player) = {
    if (forward) {
      move(player, moveSpeed)
    }
    if (back) {
      move(player, -moveSpeed)
    }
    if (left) {
      rotate(player, rotateSpeed)
    }
    if (right) {
      rotate(player, -rotateSpeed)
    }
  }

  /**
   * Rotate the player's direction ja plane vector using the rotation matrix.
   *
   *  @param player is declared in Renderer and it contains information about the position, direction and plane vectors
   *  @param speed is the rotating speed. If speed > 0, the player is rotating counterclockwise (otherwise clockwise).
   */

  def rotate(player: Player, speed: Double): Unit = {
    val oldDirX = player.dirX
    val oldDirY = player.dirY

    // New direction vector using the rotation matrix.
    val dirX = oldDirX * cos(speed) - oldDirY * sin(speed)
    val dirY = oldDirX * sin(speed) + oldDirY * cos(speed)

    val oldPlaneX = player.planeX
    val oldPlaneY = player.planeY

    // New plane vector using the rotation matrix.
    val planeX = oldPlaneX * cos(speed) - oldPlaneY * sin(speed)
    val planeY = oldPlaneX * sin(speed) + oldPlaneY * cos(speed)

    player.rotate(dirX, dirY, planeX, planeY)
  }

  /**
   * Move the player's position forward or backwards if the square in the world map doesn't contain a wall.
   *
   *  @param player is declared in Renderer and it contains information about the position, direction and plane vector
   *  @param speed how fast the player moves. If speed > 0, the player moves forward (otherwise backwards).
   */

  def move(player: Player, speed: Double): Unit = {
    // The difference between the new and old coordinates.
    val dx = player.dirX * speed
    val dy = player.dirY * speed
    
    val newX = player.posX + dx
    val newY = player.posY + dy
    // The location in the world map.
    val newXSquare = newX.toInt
    val newYSquare = newY.toInt

    // Return true if there is no wall in the square where the player wants to move. Otherwise, return false.
    def notWall(x: Double, y: Double, player: Player) = {
      val maxX = map(0).size - 1
      val maxY = map.size - 1

      // Check that there is no wall.
      var result = map(y.toInt)(x.toInt) == 0

      // If there is no wall, proceed to check the player is inside the borders of the map.
      if (result) {
        result = x > 0 && x < maxX && y > 0 && y < maxY
      }
      result
    }

    // If there is no wall, move the player.
    if (notWall(newX, newY, player)) {
      player.move(newX, newY)
    }

  }

}