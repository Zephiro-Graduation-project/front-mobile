package com.example.frontzephiro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.frontzephiro.R
import com.example.frontzephiro.models.Achievement

class AchievementAdapter(private val achievementList: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rewardText: TextView = itemView.findViewById(R.id.txt_reward)
        val streakText: TextView = itemView.findViewById(R.id.txt_streak_days)
        val descriptionText: TextView = itemView.findViewById(R.id.txt_description)
        val coinLottie: LottieAnimationView = itemView.findViewById(R.id.lottie_coin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievementList[position]
        holder.rewardText.text = achievement.reward
        holder.streakText.text = achievement.streakDays
        holder.descriptionText.text = achievement.description
        holder.coinLottie.playAnimation()
    }

    override fun getItemCount(): Int = achievementList.size
}
