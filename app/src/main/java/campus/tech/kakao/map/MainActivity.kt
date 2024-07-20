package campus.tech.kakao.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.databinding.ActivityMainBinding
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainModel: MainModel
    private lateinit var mainViewModel: MainViewModel
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainModel = MainModel(application as MyApplication)

        val viewModelFactory = MainViewModelFactory(application as MyApplication, mainModel)
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        observeInputTextChanges()

        val resultAdapter = RecyclerViewAdapter {
            mainViewModel.resultItemClickListener(it)
            moveMapView(it)
        }
        binding.recyclerView.apply {
            adapter = resultAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        mainViewModel.placeList.observe(this) { list ->
            resultAdapter.submitList(list)
            resultAdapter.notifyDataSetChanged()
        }

        mainViewModel.placeListVisible.observe(this) {
            binding.recyclerView.isVisible = it
            binding.noResultTextview.isVisible = !it
        }

        val tapAdapter = TapViewAdapter {
            mainViewModel.deleteLogClickListner(it)
        }
        binding.tabRecyclerview.apply {
            adapter = tapAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        mainViewModel.logList.observe(this) {
            tapAdapter.submitList(it)
        }

        mainViewModel.tabViewVisible.observe(this) {
            binding.tabRecyclerview.isVisible = it
        }

        binding.closeButton.setOnClickListener {
            binding.input.text.clear()
            mainViewModel.closeButtonClickListener()
        }
    }

    private fun observeInputTextChanges(){
        val inputChangeObservable = binding.input.textChanges()
        val disposable = inputChangeObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { text ->
                mainViewModel.callResultList(text.toString())
                mainViewModel.showPlaceList()
            }
        disposables.add(disposable)
    }
    private fun moveMapView(place: Place) {
        val intent = Intent(this, MapViewActivity::class.java)
        intent.putExtra("PLACE_NAME", place.name)
        intent.putExtra("PLACE_LOCATION", place.location)
        intent.putExtra("PLACE_X", place.x)
        intent.putExtra("PLACE_Y", place.y)
        startActivity(intent)
    }

    private fun saveLastLocation(context: Context) {
        val lastLocation = mainViewModel.callLogList().lastOrNull()

        val sharedPreferences = context.getSharedPreferences("LastLocation", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("PLACE_X", lastLocation?.x.toString())
            putString("PLACE_Y", lastLocation?.y.toString())
            apply()
        }
    }
    override fun onStop() {
        super.onStop()
        saveLastLocation(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}

