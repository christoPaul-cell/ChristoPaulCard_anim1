package com.example.flashcard_anim

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcard.Flashcard
import com.example.flashcard.FlashcardDatabase
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    var currentCardDisplayedIndex = 0
    lateinit var flashcardDatabase: FlashcardDatabase
    private var allFlashcards = mutableListOf<Flashcard>()

    var countDownTimer: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        flashcardDatabase = FlashcardDatabase(this)
        flashcardDatabase.initFirstCard()
        allFlashcards = flashcardDatabase.getAllCards().toMutableList()
        val isShowingAnswers = findViewById<ImageView>(R.id.toggle123)
        val flashcard_question = findViewById<TextView>(R.id.flashcard_question)
        val flashcard_reponse = findViewById<TextView>(R.id.flashcard_reponse)
        val NextButton = findViewById<ImageView>(R.id.arrow)

        val answerSideView = findViewById<View>(R.id.flashcard_reponse)
        val questionSideView = findViewById<View>(R.id.flashcard_question)


        NextButton.setOnClickListener {
            if (allFlashcards.isEmpty()) {
                return@setOnClickListener  // Il n'y a pas de cartes à afficher

            }
            // Charger les animations
            val leftOutAnim = AnimationUtils.loadAnimation(this, R.anim.left_out)
            val rightInAnim = AnimationUtils.loadAnimation(this, R.anim.right_in)

            // Définir un listener pour l'animation leftOut
            leftOutAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // Cette méthode est appelée lorsque l'animation démarre

                }

                override fun onAnimationEnd(animation: Animation?) {
                    // Cette méthode est appelée lorsque l'animation est terminée
                    // Commencez l'animation rightIn après que leftOut soit terminée
                    findViewById<View>(R.id.flashcard_question).startAnimation(rightInAnim)
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    // Nous n'avons pas besoin de nous préoccuper de cette méthode
                }
            })
            findViewById<View>(R.id.flashcard_question).startAnimation(leftOutAnim)


            currentCardDisplayedIndex++

            if (currentCardDisplayedIndex >= allFlashcards.size) {
                currentCardDisplayedIndex = 0  // Revenir à la première carte si nous avons atteint la fin
            }

            val (question, answer) = allFlashcards[currentCardDisplayedIndex]

            // Mettre à jour les TextViews avec la nouvelle carte
            flashcard_question.text = question
            flashcard_reponse.text = answer

            countDownTimer?.cancel()

            countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    findViewById<TextView>(R.id.timer).text = "" + millisUntilFinished / 1000
                }

                override fun onFinish() {}
            }.start()


        }
        flashcard_question.setOnClickListener {
            flashcard_question.visibility = View.INVISIBLE
            flashcard_reponse.visibility = View.VISIBLE

            val cx = answerSideView.width / 2
            val cy = answerSideView.height / 2
            val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
            val anim = ViewAnimationUtils.createCircularReveal(answerSideView, cx, cy, 0f, finalRadius)
            anim.duration = 2000
            answerSideView.visibility = View.VISIBLE
            questionSideView.visibility = View.INVISIBLE
            anim.start()


        }

        flashcard_reponse.setOnClickListener {
            flashcard_question.visibility = View.VISIBLE
            flashcard_reponse.visibility = View.INVISIBLE
            

        }
        isShowingAnswers.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            resultLauncher.launch(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)

        }

    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val extras = data?.extras

        if (extras != null) { // Check that we have data returned
            val question = extras.getString("question")
            val answer = extras.getString("answer")

            // Log the value of the strings for easier debugging
            Log.i("MainActivity", "question: $question")
            Log.i("MainActivity", "answer: $answer")

            // Display newly created flashcard
            findViewById<TextView>(R.id.flashcard_question).text = question
            findViewById<TextView>(R.id.flashcard_reponse).text = answer

            // Save newly created flashcard to database
            if (question != null && answer != null) {
                flashcardDatabase.insertCard(Flashcard(question, answer))
                // Update set of flashcards to include new card
                allFlashcards = flashcardDatabase.getAllCards().toMutableList()
            } else {
                Log.e("TAG", "Missing question or answer to input into database. Question is $question and answer is $answer")
            }
        } else {
            Log.i("MainActivity", "Returned null data from AddCardActivity")
        }
    }
}
