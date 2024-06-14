package com.example.Breeze

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.Breeze.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//0b17eeca625632e9920e3365aaf7ab0c

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var city = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1001)
        }
        else{
           // getlocation()
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fetchWeatherData("Indore")
        searchcity()

        


    }

    /*private fun getlocation() {
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
        fusedLocationClient.lastLocation.addOnSuccessListener{ location : android.location.Location?->
            if (location!=null){
               // getcity(location.latitude,location.longitude)

            }
        }
    }*/

   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 ){
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
               getlocation()
            }
        }
    }*/


    /*private fun getcity(lat:Double, long:Double){
        try {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val address = geoCoder.getFromLocation(lat,long,3)
            if (address!=null){
                city = address[0].locality.toString()
                Log.d("TAG", city.toString())

            }

        }catch (e:Exception){
            Toast.makeText(this, "Location Loading", Toast.LENGTH_SHORT).show()
        }

    }*/

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
                if (newText != null) {
                    fetchWeatherData(newText)
                }
                return true
            }

        })
    }


    private fun fetchWeatherData(cityname:String) {
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



