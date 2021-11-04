package com.ewam.foreheadapp.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel : ViewModel() {

    companion object {
        private const val DONE = 0L
        private const val ONE_SECOND = 1000L
        private const val COUNTDOWN_TIME = 60000L
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
    }

    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    private val timer: CountDownTimer

    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime

    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    // The current song
    private val _song = MutableLiveData<String>()
    val song: LiveData<String>
        get() = _song
    
    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    // The list of songs
    private lateinit var songList: MutableList<String>

    // Event which triggers the end of the game
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish

    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz

    init {
        resetList()
        nextSong()
        _score.value = 0

        // Creates a timer which triggers the end of the game when it finishes
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (millisUntilFinished / ONE_SECOND)
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _eventGameFinish.value = true
                _eventBuzz.value = BuzzType.GAME_OVER
            }
        }
        timer.start()
    }

    // Resets the list of songs and randomizes the order
    private fun resetList() {
        songList = mutableListOf(
            "Californication by Red Hot Chili Peppers",
            "The Queen Is Dead by The Smiths",
            "Wish You Were Here by Pink Floyd",
            "London Calling by The Clash",
            "Back In Black by AC/DC",
            "Smells Like Teen Spirit by Nirvana",
            "Imagine by John Lennon",
            "One by U2",
            "Billie Jean by Michael Jackson",
            "Bohemian Rhapsody by Queen",
            "Hey Jude by The Beatles",
            "Like A Rolling Stone by Bob Dylan",
            "I Can't Get No Satisfaction by Rolling Stones",
            "Wannabe by Spice Girls",
            "Bad Romance by Lady Gaga"
        )
        songList.shuffle()
    }

    // Moves to the next song in the list
    private fun nextSong() {
        //Select and remove a song from the list
        if (songList.isEmpty()) {
            resetList()
        }
        _song.value = songList.removeAt(0)
    }

    // Methods for button presses
    fun onSkip() {
        _score.value = (_score.value)?.minus(1)
        nextSong()
    }

    fun onCorrect() {
        _score.value = (_score.value)?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        nextSong()
    }

    // Methods for completed events
    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

}
