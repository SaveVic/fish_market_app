package ppl.asn.fishmarket

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import com.google.firebase.database.*
import ppl.asn.fishmarket.data.PreferencesData
import ppl.asn.fishmarket.ui.home.HomeSharedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController : NavController

    private lateinit var database : DatabaseReference
    private lateinit var model : HomeSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(toolbar)
        database = FirebaseDatabase.getInstance().reference.child("data")
        model = ViewModelProviders.of(this).get(HomeSharedViewModel::class.java)
        model.changeList(arrayListOf())

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_profile,
                R.id.nav_transaction,
                R.id.nav_setting
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_refresh -> actionRefresh()
            R.id.action_logout -> actionLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun actionRefresh()
    {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Refresh...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        database.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
                progressDialog.dismiss()
                Toast.makeText(this@MainActivity, "Failed to refresh", Toast.LENGTH_SHORT).show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                progressDialog.dismiss()
                try{
                    for(item in dataSnapshot.children){
                        if((item.key ?: "") != "url_main") continue
                        val urlNew = item.value as String
                        Log.e("url from firebase", urlNew)
                        PreferencesData.setURL(this@MainActivity, urlNew)
                        break
                    }
                    Toast.makeText(this@MainActivity, "Refresh done", Toast.LENGTH_SHORT).show()
                }
                catch(e : Exception){
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Exception", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun actionLogout()
    {
        PreferencesData.setRole(baseContext, "")
        PreferencesData.setMoney(baseContext, 0)
        PreferencesData.setName(baseContext, "")
        PreferencesData.setUsername(baseContext, "")
        PreferencesData.setEmail(baseContext, "")
        PreferencesData.setAddress(baseContext, "")
        PreferencesData.setPhone(baseContext, "")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

//    fun navigateBackWithResult(result: Bundle) {
//        val childFragmentManager = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager
//        var backStackListener: FragmentManager.OnBackStackChangedListener by Delegates.notNull()
//        backStackListener = FragmentManager.OnBackStackChangedListener {
//            (childFragmentManager?.fragments?.get(0) as NavigationResult).onNavigationResult(result)
//            childFragmentManager.removeOnBackStackChangedListener(backStackListener)
//        }
//        childFragmentManager?.addOnBackStackChangedListener(backStackListener)
//        navController().popBackStack()
//    }
}
