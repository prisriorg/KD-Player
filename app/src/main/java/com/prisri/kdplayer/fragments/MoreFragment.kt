package com.prisri.kdplayer.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.prisri.kdplayer.R
import com.prisri.kdplayer.activity.AboutActivity
import com.prisri.kdplayer.activity.SettingsActivity
import com.prisri.kdplayer.activity.ThemeActivity
import com.prisri.kdplayer.databinding.FragmentMoreBinding


class MoreFragment : Fragment() {
    var tglink = "KD_PLAYER"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v= inflater.inflate(R.layout.fragment_more, container, false)
        val binding = FragmentMoreBinding.bind(v)
        binding.Exit.setOnClickListener{
            getActivity()?.finish();
            requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
        binding.About.setOnClickListener {
            val intent = Intent(
                activity,
                AboutActivity::class.java           )
            intent.putExtra("appID", "About")
            startActivity(intent)
            requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
        binding.privacyPolicy
            .setOnClickListener{
                val intent = Intent(
                    activity,
                    AboutActivity::class.java
                )
                intent.putExtra("appID", "Privacy Policy")
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        binding.rateUs.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=" + requireContext().packageName)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Handle case where Google Play Store app is not available
                Toast.makeText(requireContext(), "Google Play Store app not found", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Handle other exceptions
                Toast.makeText(requireContext(), "Error opening Google Play Store", Toast.LENGTH_SHORT).show()
            }
            requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
        binding.telGrm.setOnClickListener{
            try {
                try {
                    requireActivity().packageManager.getPackageInfo("org.telegram.messenger", 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    requireActivity().packageManager
                        .getPackageInfo("org.thunderdog.challegram", 0)
                }
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("tg://resolve?domain=$tglink")
                )
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } catch (e: PackageManager.NameNotFoundException) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://telegram.me/$tglink")
                )
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }
        binding.themm.setOnClickListener{
            val intent = Intent(
                activity,
                ThemeActivity::class.java
            )
            startActivity(intent)
            requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
        binding.settings.setOnClickListener{
            val intent = Intent(
                activity,
                SettingsActivity::class.java
            )
            startActivity(intent);
        }
        return v
    }
}