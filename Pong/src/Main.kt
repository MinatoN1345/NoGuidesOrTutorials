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

class Pong : Application()
{
    var root = Pane()
    var scene = Scene(root,800.0,600.0)
    var canvas = Canvas(800.0,600.0)
    var graphicsContext : GraphicsContext = canvas.graphicsContext2D

    // Scene Items
    var labelLeftPlayer = Label("0")
    var labelRightPlayer = Label("0")

    // Paddles and Ball Declaration.
    //var leftPaddle = Rectangle(15.0,80.0)
    var paddleLeft = Paddle()
    var paddleRight = Paddle()
   // var rightPaddle = Rectangle(15.0,80.0)
    var paddleSpeed = 25
    //var ball = Sphere(4.0)
    var ball = Rectangle(5.0,5.0)
    var ballSpeed = 4 //was 2 , was 8
    var collisionDetected = false
    var outOfbounds = false
    val maxPoints : Int = 15
    var isGameOver = false
    var ballSwitchDirection = false
    var initialStart = false
    var gameOverLabel = Label("Game Over")
    var playerWinsLabel = Label()
    var startGameLabel = Label("Press 'space' to start")

    var ballHitMedia = Media(this.javaClass.getResource("Res/Audio/ballHitPaddle.wav").toExternalForm().toString())
    var ballMissedMedia = Media(this.javaClass.getResource("Res/Audio/ballMissedByPlayer.wav").toExternalForm().toString())
    var ballHitWallMedia = Media(this.javaClass.getResource("Res/Audio/ballHitWall.wav").toExternalForm().toString())
    var mediaPlayer = MediaPlayer(ballHitMedia)

    var upArrowPressed = SimpleBooleanProperty()
    var downArrowPressed = SimpleBooleanProperty()
    var wPressed = SimpleBooleanProperty()
    var sPressed = SimpleBooleanProperty()

    var animationTimer = object:  AnimationTimer()
    {
        override fun handle(now: Long) {
            moveBall()
            checkPressed()
            ballPaddleCollisionDetection()
            if(outOfbounds)
            {
                this.stop()
            }
            if(isGameOver)
            {
                this.stop()
                gameOverLabel.isVisible = true
                playerWinsLabel.isVisible = true

                var playerNumber = 0
                if(labelLeftPlayer.text.toInt() > labelRightPlayer.text.toInt())
                {
                    playerNumber = 1
                }
                else
                {
                    playerNumber = 2
                }
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
        graphicsContext.fillRect(0.0,0.0,canvas.width,canvas.height)
        root.children.add(canvas)



        scene.setOnKeyPressed{

            when(it.code)
            {
                KeyCode.W -> wPressed.set(true)
                KeyCode.S -> sPressed.set(true)
                KeyCode.UP -> upArrowPressed.set(true)
                KeyCode.DOWN -> downArrowPressed.set(true)
                KeyCode.SPACE -> if(!initialStart)
                {
                    drawGame()
                    initialStart = true
                }
                //---------------------------------------------//TODO REMOVE DELETE
                KeyCode.T -> movePaddle(ball,-paddleSpeed)
                KeyCode.G -> movePaddle(ball,paddleSpeed)
                KeyCode.F -> ball.x -= 10
                KeyCode.H -> ball.x += 10
                KeyCode.R -> restart()
                else ->{}
            }
        }

        scene.setOnKeyReleased {

            when(it.code)
            {
                KeyCode.W -> wPressed.set(false)
                KeyCode.S -> sPressed.set(false)
                KeyCode.UP -> upArrowPressed.set(false)
                KeyCode.DOWN -> downArrowPressed.set(false)
                //---------------------------------------------

                else ->{}
            }

        }


        startGameLabel.layoutX = 180.0
        startGameLabel.layoutY = 280.0
        startGameLabel.isWrapText = true
        startGameLabel.textFill = Color.WHITE
        startGameLabel.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(),36.0)
        root.children.add(startGameLabel)



    }

    fun movePaddle(paddle : Rectangle, direction: Int)
    {
        if(paddle.y + direction <= canvas.height && paddle.y + direction > 0)
        {
            paddle.y = paddle.y + direction
        }
    }

    fun spawnBall()
    {
        ball.x = 400.0
        ball.y = (Random().nextInt(500) + 100).toDouble() //Was 300.0
        outOfbounds = false
        //animationTimer.start()
    }

    fun moveBall()
    {
        if((ball.x >= 790.0) || (ball.y >= 590.0) )
        {
           ballSpeed = -ballSpeed
            ballSwitchDirection = true
            mediaPlayer = MediaPlayer(ballHitWallMedia)
            mediaPlayer.stop()
            mediaPlayer.play()
        }
        if(!ballSwitchDirection)
        {
           // ballSpeed = -ballSpeed
            ball.x = ball.x + ballSpeed
            ball.y = ball.y + ballSpeed
        }
        else
        {
            println("In here")
            println(ball.x)
            println(ball.y)
            ball.x = ball.x - ballSpeed
            ball.y = ball.y + ballSpeed
            ballSwitchDirection = false
        }


        if(ball.x <= 0 || ball.y <= 0 )
        {
            ballSpeed = -ballSpeed
            ballSwitchDirection = true // commeted out 18 57 23 10 18 may need uncommenting //TODO
            mediaPlayer = MediaPlayer(ballHitWallMedia)
            mediaPlayer.stop()
            mediaPlayer.play()
        }

        if(ball.x > paddleRight.rectangle.x)
        {
            outOfbounds = true
            if(outOfbounds)
            {
                increaseScore(labelLeftPlayer)
                spawnBall()
            }
        }

        if(ball.x < paddleLeft.rectangle.x)
        {
            outOfbounds = true
            if(outOfbounds)
            {
                increaseScore(labelRightPlayer)
               spawnBall()
            }
        }
    }

    fun drawGame()
    {

        startGameLabel.isVisible = false
        labelLeftPlayer.layoutX =200.0
        labelLeftPlayer.layoutY =40.0
        labelLeftPlayer.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(),72.0)
        labelLeftPlayer.textFill = Color.WHITE
        root.children.add(labelLeftPlayer)

        labelRightPlayer.layoutX = 600.0
        labelRightPlayer.layoutY =40.0
        ///  labelRightPlayer.font = Font.font(72.0)
        labelRightPlayer.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(),72.0)
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
        //spawnBall()
        root.children.add(ball)

        graphicsContext.fill = Color.WHITE

        for(i in 0..600 step(40))
        {
            graphicsContext.fillRect(390.0,00.0 + i,20.0,20.0)
        }

        gameOverLabel.layoutX = 200.0
        gameOverLabel.layoutY = 210.0
        gameOverLabel.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(),72.0)
        gameOverLabel.textFill = Color.RED
        gameOverLabel.isVisible = false
        root.children.add(gameOverLabel)

        playerWinsLabel.layoutX = 160.0
        playerWinsLabel.layoutY =280.0
        playerWinsLabel.font = Font.loadFont(this.javaClass.getResource("Res/Font/font.ttf").toExternalForm().toString(),72.0)
        playerWinsLabel.textFill = Color.GREEN
        playerWinsLabel.isVisible = false
        root.children.add(playerWinsLabel)

        animationTimer.start()
    }

    fun checkPressed()
    {
        if(upArrowPressed.get())
        {
            movePaddle(paddleRight.rectangle,-paddleSpeed)
        }

        if(downArrowPressed.get())
        {
            movePaddle(paddleRight.rectangle,paddleSpeed)
        }

        if(wPressed.get())
        {
            movePaddle(paddleLeft.rectangle,-paddleSpeed)
        }

        if(sPressed.get())
        {
            movePaddle(paddleLeft.rectangle,paddleSpeed)
        }
    }


    fun ballPaddleCollisionDetection()
    {
        if(ball.intersects(paddleLeft.rectangle.x,paddleLeft.rectangle.y,paddleLeft.rectangle.width,paddleLeft.rectangle.height))
        {
            mediaPlayer.stop()
            mediaPlayer.play()
            collisionDetected = true
            println("Player One hits it!")
            ballSpeed = -ballSpeed
            //ballSwitchDirection = true
        }
        if(ball.intersects(paddleRight.rectangle.x,paddleRight.rectangle.y,paddleRight.rectangle.width,paddleRight.rectangle.height))
        {
            mediaPlayer.stop()
            mediaPlayer.play()
            collisionDetected = true
            println("Player Two hits it!")
            ballSpeed = -ballSpeed
            //ballSwitchDirection = true
        }

    }

    fun increaseScore(label: Label)
    {
        mediaPlayer = MediaPlayer(ballMissedMedia)
        mediaPlayer.stop()
        mediaPlayer.play()
        val score = label.text.toInt()
        if(score + 1 <= maxPoints)
        {
            var scoreIncrement = score

             if(scoreIncrement != (score + 1))
             {
                 scoreIncrement = score + 1
             }
            label.text = scoreIncrement.toString()
        }
        else
        {
            isGameOver = true
        }

    }

    fun resetForReplay()
    {
        labelLeftPlayer.text  = "0"
        labelRightPlayer.text  = "0"
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

    fun restart()
    {
        if(isGameOver)
        {
            isGameOver = false
            resetForReplay()
            animationTimer.start()
        }
    }



}

//open class Paddle(val paddleSpeed:Int = 25, val width:Int, height: Int)
open class Paddle(val paddleSpeed:Int = 25, val rectangle: Rectangle = Rectangle(15.0, 80.0))
{

}
fun main(args: Array<String>) {
    Application.launch(Pong::class.java, *args)
}