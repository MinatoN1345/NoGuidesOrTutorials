import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.Sphere
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
    var startButton = Button("Start")
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
    var ballSpeed = 8 //was 2
    var collisionDetected = false
    var outOfbounds = false
    val maxPoints : Int = 15
    var isGameOver = false
    var gameOverLabel = Label("Game Over")
    var playerWinsLabel = Label()
    var animationTimer = object:  AnimationTimer()
    {
        override fun handle(now: Long) {
            moveBall()
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
            }
        }
    }


    override fun start(primaryStage: Stage?) {
        primaryStage?.scene = scene
        primaryStage?.title ="Pong"
        primaryStage?.show()
        graphicsContext.fill = Color.BLACK
        graphicsContext.fillRect(0.0,0.0,canvas.width,canvas.height)
        root.children.add(canvas)
        /*
             startButton.layoutX = 300.0
             startButton.layoutY = 300.0
             startButton.prefWidth=200.0
             startButton.prefHeight = 50.0
             startButton.textFill= Color.WHITE
             startButton.style = "-fx-background-color: #000000; -fx-border-width: 5px; -fx-border-color:White"
             //startButton.isVisible = false
          startButton.setOnAction {
                 startButton.isVisible = false

             }
             */

        scene.setOnKeyPressed{

            when(it.code)
            {
                KeyCode.W -> movePaddle(paddleLeft.rectangle,-paddleSpeed)
                KeyCode.S -> movePaddle(paddleLeft.rectangle,paddleSpeed)
                KeyCode.UP -> movePaddle(paddleRight.rectangle,-paddleSpeed)
                KeyCode.DOWN ->  movePaddle(paddleRight.rectangle,paddleSpeed)
                KeyCode.T -> movePaddle(ball,-paddleSpeed)
                KeyCode.G -> movePaddle(ball,paddleSpeed)
                KeyCode.F -> ball.x -= 10
                KeyCode.H -> ball.x += 10
                else ->{}
            }
        }
        //root.children.add(startButton)

        labelLeftPlayer.layoutX =200.0
        labelLeftPlayer.layoutY =40.0
        labelLeftPlayer.font = Font.font(72.0)
        labelLeftPlayer.textFill = Color.WHITE
        root.children.add(labelLeftPlayer)

        labelRightPlayer.layoutX = 600.0
        labelRightPlayer.layoutY =40.0
        labelRightPlayer.font = Font.font(72.0)
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

//        ball.layoutX = 400.0
        ball.x = 400.0
        ball.y = 300.0
  //      ball.layoutY = 300.0
        ball.fill = Color.WHITE
       // ball.style = "-fx-background-color: #FF0000; -fx-border-width: 5px; -fx-border-color:White"
       // spawnBall()
        root.children.add(ball)

        graphicsContext.fill = Color.WHITE

        for(i in 0..600 step(40))
        {
            graphicsContext.fillRect(390.0,00.0 + i,20.0,20.0)
        }

        gameOverLabel.layoutX = 200.0
        gameOverLabel.layoutY =250.0
        gameOverLabel.font = Font.font(72.0)
        gameOverLabel.textFill = Color.RED
        gameOverLabel.isVisible = false
        root.children.add(gameOverLabel)

        playerWinsLabel.layoutX = 200.0
        playerWinsLabel.layoutY =360.0
        playerWinsLabel.font = Font.font(72.0)
        playerWinsLabel.textFill = Color.GREEN
        playerWinsLabel.isVisible = false
        root.children.add(playerWinsLabel)

        animationTimer.start()

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
        ball.y = 300.0
        outOfbounds = false
        animationTimer.start()
    }

    fun moveBall()
    {
        if((ball.x >= 790.0) || (ball.y >= 590.0) )
        {
            ballSpeed = -ballSpeed
        }
        ball.x = ball.x + ballSpeed
        ball.y = ball.y + ballSpeed
        if(ball.x <= 0 || ball.y <= 0 )
        {
            ballSpeed = -ballSpeed
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

    fun ballPaddleCollisionDetection()
    {
        if(ball.intersects(paddleLeft.rectangle.x,paddleLeft.rectangle.y,paddleLeft.rectangle.width,paddleLeft.rectangle.height))
        {
            collisionDetected = true
            println("Player One hits it!")
            ballSpeed = -ballSpeed
        }
        if(ball.intersects(paddleRight.rectangle.x,paddleRight.rectangle.y,paddleRight.rectangle.width,paddleRight.rectangle.height))
        {
            collisionDetected = true
            println("Player Two hits it!")
            ballSpeed = -ballSpeed
        }

    }

    fun increaseScore(label: Label)
    {

        val score = label.text.toInt()
        if(score + 1 <= maxPoints)
        {
            var scoreIncrement = score
            while(scoreIncrement != score + 1)
            {
                scoreIncrement = score + 1
            }
            /* if(scoreIncrement != (score + 1))
             {
                 scoreIncrement = score + 1
             }*/
            label.text = scoreIncrement.toString()
        }
        else
        {
            isGameOver = true
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