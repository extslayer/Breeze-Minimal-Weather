package com.example.Breeze

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.Breeze.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

//0b17eeca625632e9920e3365aaf7ab0c

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private var cityn = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        Log.d("ct", "$cityn")






        fetchWeatherData("Indore")
        searchcity()

        


    }


    private fun getcity(lat : Double, long: Double):String{
        var city = ""
        var geocoder = Geocoder(this, Locale.getDefault())
        var adress = geocoder.getFromLocation(lat,long,1)
        city = adress!!.get(0).locality

        return city
    }

    private fun getLastLocation(){
        if (checkPermission()){
            if (isLocationEnabled()){

                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location = task.result
                    if (location == null){
                        getnewlocation()

                    }else{

                        cityn = getcity(location.latitude,location.longitude)

                    }
                }

            }else{
                Toast.makeText(this, "Please Enable your GPS", Toast.LENGTH_LONG).show()
            }

        }
        else{
            RequestPermission()
        }

    }


    private fun getnewlocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,locationCallback,Looper.myLooper()
        )
    }

    private val locationCallback = object :LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            cityn = getcity(lastLocation!!.latitude,lastLocation.longitude)
        }
    }


    private fun checkPermission():Boolean{
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),1000)
    }

    private fun isLocationEnabled(): Boolean {

        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000){
            if (grantResults.isNotEmpty()&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug:", " u have permission")
            }
        }
    }




    private fun searchcity() {
        val searchview = binding.searchView
        searchview.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                
                return true
            }

        })
    }


    private fun fetchWeatherData(cityname:String="India") {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        val response =
            retrofit.getWeatherData(cityname, "0b17eeca625632e9920e3365aaf7ab0c", "metric")
        //Log.d("TAG", response.toString())
        response.enqueue(object :Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if(response.isSuccessful && responseBody!=null){
                    val temperature = responseBody.main.temp.toString()
                    val high = responseBody.main.temp_max.toString()
                    val low = responseBody.main.temp_min.toString()
                    val humidity = responseBody.main.humidity.toString()
                    val windspeed = responseBody.wind.speed.toString()
                    val pressure = responseBody.main.pressure.toInt().toString()
                    val condition = responseBody.weather.firstOrNull()?.main ?:"unknown"
                    val description = responseBody.weather.firstOrNull()?.description ?:"unknown"
                    val feelslike = responseBody.main.feels_like.toString()
                    binding.temp.text = ("$temperature")
                    binding.high.text = ("$high"+"°C")
                    binding.low.text = ("$low"+"°C")
                    binding.humidity.text = ("$humidity" + " %")
                    binding.windspeed.text = ("$windspeed" + " m/s")
                    binding.pressure.text = ("$pressure"+ " mBar")
                    binding.city.text = ("$cityname"+",")
                    binding.condition.text = ("$description")
                    binding.feels.text = ("Feels Like: "+"$feelslike"+"°C")
                    changeacctocondition(condition)

                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun changeacctocondition(conditions : String) {
        when(conditions){
            "Clouds","Mist","Haze","Fog"  ->{
                binding.lotteview.setAnimation(R.raw.cloud)
            }
            "Clear" ->{
                binding.lotteview.setAnimation(R.raw.sun)
            }
            "Thunderstorm" ->{
                binding.lotteview.setAnimation(R.raw.thunder)
            }
            "Drizzle" ->{
                binding.lotteview.setAnimation(R.raw.rain)
            }
            "Rain" ->{
                binding.lotteview.setAnimation(R.raw.rain)
            }
            "Snow" ->{
                binding.lotteview.setAnimation(R.raw.snow3)
            }
            "clear" ->{
                binding.lotteview.setAnimation(R.raw.sun)
            }


        }
        binding.lotteview.playAnimation()
    }
}



