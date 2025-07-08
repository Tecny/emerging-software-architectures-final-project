package com.example.dtaquito.sports

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.dtaquito.R
import com.example.dtaquito.gameroom.GameRoomFragment

class SportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_sport, container, false)
    }

    private fun openGameRoom(sportType: String) {
        val view = view ?: return
        val billarArea = view.findViewById<View>(R.id.billar_area)
        val futbolArea = view.findViewById<View>(R.id.futbol_area)
        val poolImg = view.findViewById<View>(R.id.pool_img)
        val soccerImg = view.findViewById<View>(R.id.soccer_img)

        // Crear animaciones para desplazar las áreas y las imágenes
        val animDuration = 500L

        // Animaciones para las áreas triangulares
        val billarSlide = ObjectAnimator.ofFloat(billarArea, "translationX", 0f, -billarArea.width.toFloat())
        billarSlide.duration = animDuration
        billarSlide.interpolator = AccelerateInterpolator()

        val futbolSlide = ObjectAnimator.ofFloat(futbolArea, "translationX", 0f, futbolArea.width.toFloat())
        futbolSlide.duration = animDuration
        futbolSlide.interpolator = AccelerateInterpolator()

        // Animaciones para las imágenes
        val poolImgSlide = ObjectAnimator.ofFloat(poolImg, "translationX", 0f, -poolImg.width.toFloat())
        poolImgSlide.duration = animDuration
        poolImgSlide.interpolator = AccelerateInterpolator()

        val soccerImgSlide = ObjectAnimator.ofFloat(soccerImg, "translationX", 0f, soccerImg.width.toFloat())
        soccerImgSlide.duration = animDuration
        soccerImgSlide.interpolator = AccelerateInterpolator()

        // Ejecutar animaciones
        val animSet = AnimatorSet()
        animSet.playTogether(billarSlide, futbolSlide, poolImgSlide, soccerImgSlide)
        animSet.start()

        // Mostrar GameRoomFragment
        view.postDelayed({
            val fragment = GameRoomFragment().apply {
                arguments = Bundle().apply {
                    putString("SPORT_TYPE", sportType)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }, animDuration)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Solo hacemos clickeables las imágenes
        view.findViewById<View>(R.id.pool_img).setOnClickListener {
            openGameRoom("BILLAR")
        }

        view.findViewById<View>(R.id.soccer_img).setOnClickListener {
            openGameRoom("FUTBOL")
        }
    }
}