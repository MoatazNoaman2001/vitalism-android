package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model


enum class Gender { male, female, UnKnown }
enum class BloodTypes { A_positive, A_negative, B_positive, B_nigative, O_positive, O_Nigative, AB_positive, AB_Nigative, UnKnown }
enum class UserType { Patient, Medical }
enum class MedicalSpecialist {
    GeneralPractitioner, General_Surgeon, Internal_Medicine_Specialist, Obstetrician_and_Gynecologist, Nutritionist, Orthopedic_Surgeon, Pediatrician, Neurologist, Oncologist, Ophthalmologist, Dermatologist, Anesthesiologist, Physical_Therapist, Pulmonologist, Cardiologist, Nephrologist, Sleep_Medicine_Specialist, Chiropractor, Sports_Medicine_Specialist, Dentist, Orthodontist, Oral_and_Maxillofacial, Surgeon, Others
}


data class UserInfo(
    var name: String,
    val email: String,
    var uid: String,
    var token: String,
    var userType: String?,
    var Disc: String? = "",
    var BirthDay: String? = null,
    var BloodType: String? = BloodTypes.UnKnown.name,
    var Diagnoses: String? = null,
    var country: String? = null,
    var profileImgLoc: String? = null,
    var profileImgUri: String? = null,
    var gender: String = Gender.UnKnown.name,
    var weight: Float = 0.0f,
    var height: Float = 0.0f,
    var phoneNumber: String? = "",
    var city: String? = "",
    var userName: String? = "",
    var license_number:String = "",
    var medicine_specialty:String = ""
) : java.io.Serializable {
    constructor() : this(
        name = "",
        email = "",
        BirthDay = "",
        BloodType = "",
        Diagnoses = "",
        country = "",
        profileImgLoc = "",
        uid = "",
        token = "",
        userType = "",
        height = 0.0f,
        gender = Gender.UnKnown.name,
        weight = 0.0f,
        phoneNumber = "",
        userName = ""
    )
}