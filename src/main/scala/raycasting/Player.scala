package raycasting

/**
 * Instances of the player class represent the person who is in the room.
 * 
 * @constructor creates a new player
 */

class Player {
  // Player's position
  private var positionX: Double = 11
  private var positionY: Double = 10
  // direction vector
  private var directionX: Double = -1
  private var directionY: Double = 0
  // the 2d raycaster version of camera plane
  private var xPlane: Double = 0
  private var yPlane: Double = 0.66
  
  // Access the player's data using these getter methods.
  def posX = this.positionX
  def posY = this.positionY
  def dirX = this.directionX
  def dirY = this.directionY
  def planeX = this.xPlane
  def planeY = this.yPlane
  
  /**
   * Rotate the player's direction and plane vector. The actual calculations considering the rotation are carried out in the 
   * Screen class so this method expects the new vector components passed as parameters to be correct.
   * 
   * @param newXDIr new x-component for the direction vector
   * @param newYDIr new y-component for the direction vector
   * @param newPlaneX new x-component for the plane vector
   * @param newPlaneY new y-component for the plane vector
   */
  
  def rotate(newXDir: Double, newYDir: Double, newPlaneX: Double, newPlaneY: Double) = {
    this.directionX = newXDir
    this.directionY = newYDir
    this.xPlane = newPlaneX
    this.yPlane = newPlaneY
  }
  
  /**
   * Move the player to a new location. The calculations are carried out in the Screen class. Hence, this method expects the 
   * new coordinates to be inside the borders of the world map and in a square where there is no wall.
   */
  def move(newX: Double, newY: Double) = {
    this.positionX = newX
    this.positionY = newY
  }
}