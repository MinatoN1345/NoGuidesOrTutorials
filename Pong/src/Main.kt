import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.layout.Pane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.stage.Stage
import java.util.*

class Pong : Application() {
    // Main components
    val root = Pane()
    val scene = Scene(root, 800.0, 600.0)
    val canvas = Canvas(800.0, 600.0)
    val graphicsContext: GraphicsContext = canvas.graphicsContext2D

    // Labels
    val labelLeftPlayer = Label("0")
    val labelRightPlayer = Label("0")
    val gameOverLabel = Label("Game Over")
    val playerWinsLabel = Label()
    val startGameLabel = Label("Press 'space' to start")

    // Booleans
    var collisionDetected = false
    var outOfbounds = false
    var isGameOver = false
    var ballSwitchDirection = false
    var initialStart = false
    var upArrowPressed = SimpleBooleanProperty()
    var downArrowPressed = SimpleBooleanProperty()
    var wPressed = SimpleBooleanProperty()
    var sPressed = SimpleBooleanProperty()
    var bottomHalfHit = false
    var topHalfHit = false

    // Psuedo Boolean
    var hitLast = "paddleLeft"
    var halfCurrentlyIn = "leftSide"

    // Paddles and Ball Declaration.
    //var leftPaddle = Rectangle(15.0,80.0)
    val paddleLeft = Paddle(name = "paddleLeft")
    val paddleRight = Paddle(name = "paddleRight")
    // var rightPaddle = Rectangle(15.0,80.0)
    var paddleSpeed = 10 //was 25
    //var ball = Sphere(4.0)
    val ball = Rectangle(5.0, 5.0)
    var ballSpeed = 4 //was 2 , was 8
    val maxPoints: Int = 15

    var bouncingBack = false
    var isBallMoving = false

    // Media (Audio Files)
    val ballHitMedia = Media(this.javaClass.getResource("Res/Audio/ballHitPaddle.wav").toExternalForm().toString())
    val ballMissedMedia = Media(this.javaClass.getResource("Res/Audio/ballMissedByPlayer.wav").toExternalForm().toString())
    val ballHitWallMedia = Media(this.javaClass.getResource("Res/Audio/ballHitWall.wav").toExternalForm().toString())
    var mediaPlayer = MediaPlayer(ballHitMedia)

    // Main Game Loop
    var animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            moveBall()
            checkPressed()
            checkWhichHalfBallIsIn()
            ballPaddleCollisionDetection()
            ballReflectDirection(hitLast)
            if (outOfbounds) {
                this.stop()
            }
            if (isGameOver) {
                this.stop()
                gameOverLabel.isVisible = true
                playerWinsLabel.isVisible = true
                val playerNumber = if (labelLeftPlayer.text.toInt() > labelRightPlayer.text.toInt()) 1 else 2
                playerWinsLabel.text = "Player " + playerNumber + " wins!"
                startGameLabel.isVisible = true
                startGameLabel.layoutX = 176.0
                startGameLabel.layoutY = 370.0
                startGameLabel.text = "Press 'r' to play again."
            }
        }
    }

    override fun start(primaryStage: Stage?) {
        primaryStage?.scene = scene
        primaryStage?.title = "Pong"
        primaryStage?.show()
        graphicsContext.fill = Color.BLACK
        graphicsContext.fillRect(0.0, 0.0, canvas.width, canvas.height)
        root.children.add(canvas)

        scene.setOnKeyPressed {

            when (it.code) {
                KeyCode.W -> wPressed.set(true)
                KeyCode.S -> sPressed.set(true)
                KeyCode.UP -> upArrowPressed.set(true)
                KeyCode.DOWN -> downArrowPressed.set(true)
                KeyCode.SPACE -> if (!initialStart) {
                    drawGame()
                    initialStart = true // Stops the user from pressing space again.
                }
                //---------------------------------------------//TODO REMOVE DELETE
                KeyCode.T -> movePaddle(ball, -paddleSpeed)
                KeyCode.G -> movePaddle(ball, paddleSpeed)
                KeyCode.F -> ball.x -= 10
                KeyCode.H -> ball.x += 10
                KeyCode.R -> restart()
                else -> {
                }
            }
        }

        scene.setOnKeyReleased {
            // When keys are released let the 'listeners' know.

            when (it.code) {
                KeyCode.W -> wPressed.set(false)
                KeyCode.S -> sPressed.set(false)
                KeyCode.UP -> upArrowPressed.set(false)
                KeyCode.DOWN -> downArrowPressed.set(false)
                else -> {
                }
            }
        }

        startGameLabel.layoutX = 180.0
        startGameLabel.layoutY = 280.0
        startGameLabel.isWrapText = true
        startGameLabel.textFill = Color.WHITE
        startGameLabel.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(), 36.0)
        root.children.add(startGameLabel)
    }

    fun movePaddle(paddle: Rectangle, direction: Int) {
        if (paddle.y + direction <= canvas.height && paddle.y + direction > 0) {
            paddle.y = paddle.y + direction
        }
    }

    fun spawnBall() // Spawns the ball when a point has been scored or at the start of the game.
    {
        ball.x = 400.0
        ball.y = (Random().nextInt(500) + 90).toDouble() //Was 300.0
        outOfbounds = false
    }

    fun moveBall() {
        isBallMoving = true
        if (!ballSwitchDirection && !bouncingBack && isBallMoving) // If you do not need to switch the direction of the ball, keep moving.
        {
            // ballSpeed = -ballSpeed

            //TODO JUST FOR HIGHLIGHTING PURPOSES :GO DOWN
           // ball.x = ball.x + ballSpeed
           // ball.y = ball.y + ballSpeed
            println("Don't switch")
        }
        if (bouncingBack) // If you need to switch the direction of the ball.
        {
            ballSwitchDirection = false
            //bouncingBack = false
            println("In here")
            println(ball.x)
            println(ball.y)
            //if(ball.y)
            if (!isBallMoving) {
                //TODO JUST FOR HIGHLIGHTING PURPOSES :GO UP
             //   ball.x = ball.x + ballSpeed
               // ball.y = ball.y - ballSpeed
            } else {
               // ball.x = ball.x + ballSpeed
              //  ball.y = ball.y - ballSpeed
            }

            //  ballSwitchDirection = false // commented out 18 59 25 10 18 may need uncommenting //TODO
        }

        if (((ball.x >= 790.0) || (ball.y >= 590.0)) && !bouncingBack) // This is checking whether ball is hitting the 'walls and ceiling' of the game.
        {
            if (!isBallMoving) {
              //  ballSpeed = -ballSpeed // Make the ball go in the opposite direction.
            }


            ballSwitchDirection = true // It's direction has been switched.
            bouncingBack = true
            isBallMoving = true
            println("Culprit")
            playSound(ballHitWallMedia) // Play the relevant sound.
        }



        if (ball.x <= 0 || ball.y <= 0) // When it hits the top is in her so the deflection logic belong here //TODO ADD DEFLECTION LOGIC[
        {
           // ballSpeed = -ballSpeed // commented out 18 57 25 10 18 may need uncommenting //TODO
            ballSwitchDirection = true // commented out 18 57 23 10 18 may need uncommenting //TODO
            //bouncingBack = true
            //// isBallMoving =true

         //   if(hitl)
           // {

           // }

            println("Error")
            playSound(ballHitWallMedia)
        }

        /////////////////////////////////////////////////////////////////////////// TODO BENEATH HERE IS FINE
        if (ball.x < paddleLeft.rectangle.x) // If the player on the left misses the ball.
        {
            outOfbounds = true
            if (outOfbounds) {
                increaseScore(labelRightPlayer)
                spawnBall()
            }
        }

        if (ball.x > paddleRight.rectangle.x) // If the player on the right misses the ball.
        {
            outOfbounds = true
            if (outOfbounds) {
                increaseScore(labelLeftPlayer)
                spawnBall()
            }
        }
    }

    fun drawGame() {
        startGameLabel.isVisible = false
        labelLeftPlayer.layoutX = 200.0
        labelLeftPlayer.layoutY = 40.0
        labelLeftPlayer.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(), 72.0)
        labelLeftPlayer.textFill = Color.WHITE
        root.children.add(labelLeftPlayer)

        labelRightPlayer.layoutX = 600.0
        labelRightPlayer.layoutY = 40.0
        labelRightPlayer.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(), 72.0)
        labelRightPlayer.textFill = Color.WHITE
        root.children.add(labelRightPlayer)

        paddleLeft.rectangle.x = 200.0
        paddleLeft.rectangle.y = 250.0
        paddleLeft.rectangle.fill = Color.WHITE
        root.children.add(paddleLeft.rectangle)

        paddleRight.rectangle.x = 600.0
        paddleRight.rectangle.y = 250.0
        paddleLeft.rectangle.fill = Color.WHITE

        paddleRight.rectangle.fill = Color.WHITE
        root.children.add(paddleRight.rectangle)

        ball.x = 400.0
        ball.y = 300.0
        ball.fill = Color.WHITE
        root.children.add(ball)

        graphicsContext.fill = Color.WHITE

        for (i in 0..600 step (40)) {
            graphicsContext.fillRect(390.0, 00.0 + i, 20.0, 20.0)
        }

        gameOverLabel.layoutX = 200.0
        gameOverLabel.layoutY = 210.0
        gameOverLabel.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(), 72.0)
        gameOverLabel.textFill = Color.RED
        gameOverLabel.isVisible = false
        root.children.add(gameOverLabel)

        playerWinsLabel.layoutX = 160.0
        playerWinsLabel.layoutY = 280.0
        playerWinsLabel.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(), 72.0)
        playerWinsLabel.textFill = Color.GREEN
        playerWinsLabel.isVisible = false
        root.children.add(playerWinsLabel)

        animationTimer.start()
    }

    fun checkPressed() // Checks whether any of the 4 keys dedicated to moving the paddles up or down, have been pressed.
    {
        if (upArrowPressed.get()) {
            movePaddle(paddleRight.rectangle, -paddleSpeed)
        }

        if (downArrowPressed.get()) {
            movePaddle(paddleRight.rectangle, paddleSpeed)
        }

        if (wPressed.get()) {
            movePaddle(paddleLeft.rectangle, -paddleSpeed)
        }

        if (sPressed.get()) {
            movePaddle(paddleLeft.rectangle, paddleSpeed)
        }
    }

    fun checkWhereBallHitPaddle(paddleStartPosition: Double)
    {
        var totalHeight = paddleStartPosition + 80.0 //paddleLeft.rectangle.height
        var positionHit = ball.y
        var halfway = (totalHeight + paddleStartPosition ) / 2

        if (halfway <= positionHit) {
            //println("Top Half")
            topHalfHit = true
            bottomHalfHit = false
        } else {
            //println("Bottom Half")
            bottomHalfHit = true
            topHalfHit = false
        }
    }

    fun checkWhichHalfBallIsIn()
    {
        if(ball.x > 390.0)
        {
            halfCurrentlyIn = "rightSide"
        }
        else
        {
            halfCurrentlyIn = "leftSide"
        }
    }

    fun ballReflectDirection(paddleName: String)
    {
        /*
            For the PaddleLeft, ball.x is always increasing.
            For the PaddleRight ball.x is always decreasing.

         */
          if(paddleName.equals("paddleLeft"))
          {
              println(ball.x)
              ball.x += ballSpeed
              if(topHalfHit)
              {
                  ball.y += ballSpeed
              }
              else if(bottomHalfHit) // As in the top of the paddle
              {
                  ball.y -= ballSpeed
              }
          }
          else if(paddleName.equals("paddleRight"))
          {
              ball.x -= ballSpeed

              if(topHalfHit)
              {
                  ball.y += ballSpeed //TODO Double check this.
              }
              else if(bottomHalfHit)
              {
                  ball.y -= ballSpeed
              }
          }
       /* else if(bottomHalfHit) // As in the top of the paddle
        {
            if(paddleName.equals("PaddleLeft"))
            {
                ball.x += ballSpeed
                ball.y -= ballSpeed
            }
            else if(paddleName.equals("PaddleRight"))
            {
                ball.x -= ballSpeed
                ball.y -= ballSpeed
            }
        } */
    }

    fun ballPaddleCollisionDetection() {
        if (ball.intersects(paddleLeft.rectangle.x, paddleLeft.rectangle.y, paddleLeft.rectangle.width, paddleLeft.rectangle.height)) {
            playSound(ballHitMedia)
            collisionDetected = true
            checkWhereBallHitPaddle(paddleLeft.rectangle.y)
            //ballReflectDirection(paddleLeft.name)
            hitLast = paddleLeft.name
            //ballSpeed = -ballSpeed
           // ballSwitchDirection = true
        }
        if (ball.intersects(paddleRight.rectangle.x, paddleRight.rectangle.y, paddleRight.rectangle.width, paddleRight.rectangle.height)) {
            playSound(ballHitMedia)
            collisionDetected = true
            checkWhereBallHitPaddle(paddleRight.rectangle.y)
            //ballReflectDirection(paddleRight.name)
            hitLast = paddleRight.name
            println("Player Two hits it! at: ${ball.x} and ${ball.y}")
            //ballSpeed = -ballSpeed
           // ballSwitchDirection = true
        }
    }

    fun increaseScore(label: Label) // Deals with increasing the score of each player.
    {
        playSound(ballMissedMedia)
        val score = label.text.toInt()
        if (score + 1 <= maxPoints) {
            var scoreIncrement = score

            if (scoreIncrement != (score + 1)) {
                scoreIncrement = score + 1
            }
            label.text = scoreIncrement.toString()
        } else {
            isGameOver = true
        }
    }

    fun resetForReplay() // Resets the game when replay is called.
    {
        labelLeftPlayer.text = "0"
        labelRightPlayer.text = "0"
        playerWinsLabel.isVisible = false
        gameOverLabel.isVisible = false
        startGameLabel.isVisible = false
        paddleLeft.rectangle.x = 200.0
        paddleLeft.rectangle.y = 250.0
        paddleRight.rectangle.x = 600.0
        paddleRight.rectangle.y = 250.0
        spawnBall()
        collisionDetected = false
        outOfbounds = false
        ballSwitchDirection = false
    }

    fun restart() // Restarts the game.
    {
        if (isGameOver) {
            isGameOver = false
            resetForReplay()
            animationTimer.start()
        }
    }

    fun playSound(media: Media) // Plays the given media depending on the situation.
    {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer(media)
        }
        mediaPlayer.stop()
        mediaPlayer.play()
    }
}

//open class Paddle(val paddleSpeed:Int = 25, val width:Int, height: Int)
open class Paddle(val paddleSpeed:Int = 25, val rectangle: Rectangle = Rectangle(15.0, 80.0), val name: String)

fun main(args: Array<String>) {
    Application.launch(Pong::class.java, *args)
} // Main function that initialises everything.