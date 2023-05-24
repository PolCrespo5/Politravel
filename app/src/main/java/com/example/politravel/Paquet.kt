package com.example.politravel

import java.io.Serializable

class Paquet (val imatge: String, val nom: String, val numDies: Int, val mitjaTransport: String, var recorregutPaquet: MutableList<Lloc>): Serializable