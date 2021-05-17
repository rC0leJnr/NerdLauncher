package com.rick.nerdlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rick.nerdlauncher.databinding.ActivityMainBinding
import com.rick.nerdlauncher.databinding.ListItemBinding

private const val TAG = "NerdLauncherActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var itemBinding: ListItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setupAdapter()
        }
    }

    private fun setupAdapter(){
        val startUpIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // this returns a list containing the ResolveInfo for all the activities that have
        // a filter matcing the given intent.
        // you can modify the result by passing a flag
        val activities = packageManager.queryIntentActivities(startUpIntent, 0)
        // This sorts the activities in alphabetichal order
        activities.sortWith(Comparator{a, b ->
            String.CASE_INSENSITIVE_ORDER.compare(
                a.loadLabel(packageManager).toString(),
                b.loadLabel(packageManager).toString()
            )
        })

        binding.recyclerView.adapter = ActivityAdapter(activities)
    }

    private inner class ActivityHolder(binding: ListItemBinding)
        :RecyclerView.ViewHolder(binding.root),
            View.OnClickListener{
                private lateinit var resolveInfo: ResolveInfo

                init {
                    itemView.setOnClickListener ( this )
                }

        // You can find the labels for the activities, along with other metadata, in
        // the resolveInfo objects that the packageManager returned.
        fun bindActivity(resolveInfo: ResolveInfo) {
            this.resolveInfo = resolveInfo
            // Keep in mind that each itemView in this app is another app
            // and therefor is ok to call itemView.context.packageManager.
            val packageManager = itemView.context.packageManager
            val appName = resolveInfo.loadLabel(packageManager).toString()
            itemBinding.textView.text = appName
            itemBinding.imageView.setImageDrawable(resolveInfo.loadIcon(packageManager))
        }

        override fun onClick(v: View?) {
            // Onclick get the info from the app selected by the user
            // load the app info and startActivity.
            val activityInfo = resolveInfo.activityInfo
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(activityInfo.applicationInfo.packageName,
                    activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val context = v!!.context
            context.startActivity(intent)
        }
            }

    private inner class ActivityAdapter(val activities: List<ResolveInfo>):
            RecyclerView.Adapter<ActivityHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
            itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context))
            return ActivityHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
            val resolveInfo = activities.get(position)
            holder.bindActivity(resolveInfo)
        }

        override fun getItemCount() = activities.size
    }

}