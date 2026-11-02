package com.mihnati.app.ui.theme

object MoroccanLocalization {
    private val isAr: Boolean
        get() = LanguageManager.isArabic

    fun localizedOrg(org: String): String {
        if (!isAr) return org
        val lower = org.lowercase()
        return when {
            lower.contains("économie") || lower.contains("economie") || lower.contains("finances") || lower.contains("mef") ->
                "وزارة الاقتصاد والمالية"
            lower.contains("intérieur") || lower.contains("interieur") ->
                "وزارة الداخلية"
            lower.contains("éducation") || lower.contains("education") || lower.contains("enseignement") || lower.contains("men") ->
                "وزارة التربية الوطنية والتعليم الأولي"
            lower.contains("santé") || lower.contains("sante") ->
                "وزارة الصحة والحماية الاجتماعية"
            lower.contains("justice") ->
                "وزارة العدل"
            lower.contains("equipement") || lower.contains("équipement") || lower.contains("eau") ->
                "وزارة التجهيز والماء"
            lower.contains("douane") ->
                "إدارة الجمارك والضرائب غير المباشرة"
            lower.contains("commissariat") || lower.contains("hcp") ->
                "المندوبية السامية للتخطيط (HCP)"
            lower.contains("entraide") ->
                "مؤسسة التعاون الوطني"
            lower.contains("dgsn") || lower.contains("sûreté") || lower.contains("surete") || lower.contains("police") ->
                "المديرية العامة للأمن الوطني (DGSN)"
            lower.contains("conservation") || lower.contains("foncière") || lower.contains("ancfcc") ->
                "الوكالة الوطنية للمحافظة العقارية (ANCFCC)"
            lower.contains("ocp") ->
                "المكتب الشريف للفوسفاط (OCP)"
            lower.contains("oncf") ->
                "المكتب الوطني للسكك الحديدية (ONCF)"
            lower.contains("royal air maroc") || lower.contains("ram") ->
                "الخطوط الملكية المغربية (RAM)"
            lower.contains("cdg") ->
                "صندوق الإيداع والتدبير (CDG)"
            lower.contains("barid") || lower.contains("poste maroc") ->
                "بريد المغرب"
            lower.contains("maroc telecom") || lower.contains("iam") ->
                "اتصالات المغرب"
            lower.contains("attijariwafa") ->
                "التجاري وفا بنك"
            lower.contains("banque populaire") || lower.contains("bcp") ->
                "البنك الشعبي"
            lower.contains("bank of africa") || lower.contains("bmce") ->
                "بنك أوف أفريقيا"
            lower.contains("cih") ->
                "بنك القرض العقاري والسياحي (CIH)"
            lower.contains("ofppt") ->
                "مكتب التكوين المهني وإنعاش الشغل (OFPPT)"
            lower.contains("anapec") ->
                "الوكالة الوطنية لإنعاش التشغيل والكفاءات (ANAPEC)"
            lower.contains("cnesten") ->
                "المركز الوطني للطاقة والعلوم والتقنيات النووية"
            lower.contains("onep") || lower.contains("onee") ->
                "المكتب الوطني للكهرباء والماء الصالح للشرب"
            lower.contains("cnrst") || lower.contains("bourse khawarizmi") ->
                "المركز الوطني للبحث العلمي والتقني (CNRST)"
            lower.contains("inra") ->
                "المعهد الوطني للبحث الزراعي"
            lower.contains("université") || lower.contains("universite") ->
                "الجامعة المغربية"
            else -> org
        }
    }

    fun localizedCity(city: String): String {
        if (!isAr) return city
        val lower = city.lowercase()
        return when {
            lower.contains("rabat") -> "الرباط"
            lower.contains("casablanca") -> "الدار البيضاء"
            lower.contains("tanger") -> "طنجة"
            lower.contains("marrakech") -> "مراكش"
            lower.contains("fès") || lower.contains("fes") -> "فاس"
            lower.contains("agadir") -> "أكادير"
            lower.contains("oujda") -> "وجدة"
            lower.contains("tétouan") || lower.contains("tetouan") -> "تطوان"
            lower.contains("meknès") || lower.contains("meknes") -> "مكناس"
            lower.contains("kénitra") || lower.contains("kenitra") -> "القنيطرة"
            lower.contains("laâyoune") || lower.contains("laayoune") -> "العيون"
            lower.contains("dakhla") -> "الداخلة"
            lower.contains("béni mellal") || lower.contains("beni mellal") -> "بني ملال"
            lower.contains("el jadida") || lower.contains("jadida") -> "الجديدة"
            lower.contains("safi") -> "آسفي"
            lower.contains("nador") -> "الناظور"
            lower.contains("khouribga") -> "خريبكة"
            lower.contains("settat") -> "سطات"
            lower.contains("national") || lower.contains("tout le maroc") -> "كافة التراب الوطني"
            else -> city
        }
    }

    fun localizedDegree(deg: String): String {
        if (!isAr) return deg
        val lower = deg.lowercase()
        return when {
            lower.contains("bac+5") || lower.contains("master") || lower.contains("ingénieur") || lower.contains("ingenieur") ->
                "باك+5 / ماستر / مهندس دولة"
            lower.contains("bac+3") || lower.contains("licence") ->
                "باك+3 / إجازة أساسية أو مهنية"
            lower.contains("bac+2") || lower.contains("dut") || lower.contains("bts") || lower.contains("technicien") || lower.contains("dts") ->
                "باك+2 / تقني متخصص (DTS/DUT)"
            lower.contains("doctorat") || lower.contains("phd") ->
                "دكتوراه / باحث"
            lower.contains("bac") ->
                "شهادة البكالوريا"
            else -> deg
        }
    }

    fun localizedTitle(title: String): String {
        if (!isAr) return title
        var result = title
        result = result.replace("Avis de concours de recrutement de", "إعلان مباراة توظيف")
        result = result.replace("Avis de concours de recrutement d'", "إعلان مباراة توظيف")
        result = result.replace("Avis de concours", "إعلان مباراة")
        result = result.replace("Concours de recrutement des", "مباراة توظيف")
        result = result.replace("Concours de recrutement de", "مباراة توظيف")
        result = result.replace("Concours de recrutement d'", "مباراة توظيف")
        result = result.replace("Concours de recrutement", "مباراة توظيف")
        result = result.replace("Concours national", "مباراة وطنية")
        result = result.replace("Concours", "مباراة")
        result = result.replace("National •", "الوطنية •")
        result = result.replace("National", "الوطنية")
        
        result = result.replace("Administrateurs 2ème grade", "متصرفين من الدرجة الثانية (السلم 11)")
        result = result.replace("Administrateur 2ème grade", "متصرف من الدرجة الثانية (السلم 11)")
        result = result.replace("Administrateurs 3ème grade", "متصرفين من الدرجة الثالثة (السلم 10)")
        result = result.replace("Administrateur 3ème grade", "متصرف من الدرجة الثالثة (السلم 10)")
        result = result.replace("Ingénieurs d'État 1er grade", "مهندسي دولة من الدرجة الأولى (السلم 11)")
        result = result.replace("Ingénieur d'État 1er grade", "مهندس دولة من الدرجة الأولى (السلم 11)")
        result = result.replace("Ingénieurs d'État", "مهندسي دولة (السلم 11)")
        result = result.replace("Ingénieur d'État", "مهندس دولة (السلم 11)")
        result = result.replace("Techniciens 3ème grade", "تقنيين من الدرجة الثالثة (السلم 9)")
        result = result.replace("Technicien 3ème grade", "تقني من الدرجة الثالثة (السلم 9)")
        result = result.replace("Techniciens 4ème grade", "تقنيين من الدرجة الرابعة (السلم 8)")
        result = result.replace("Technicien 4ème grade", "تقني من الدرجة الرابعة (السلم 8)")
        result = result.replace("Inspecteurs des Douanes", "مفتشي إدارة الجمارك")
        result = result.replace("Inspecteur des Douanes", "مفتش إدارة الجمارك")
        result = result.replace("Inspecteurs", "مفتشين")
        result = result.replace("Inspecteur", "مفتش")
        result = result.replace("Officiers de Police", "ضباط الشرطة")
        result = result.replace("Officier de Police", "ضابط الشرطة")
        result = result.replace("Commissaires de Police", "عمداء الشرطة")
        result = result.replace("Commissaire de Police", "عميد شرطة")
        result = result.replace("Gardiens de la Paix", "حراس الأمن")
        result = result.replace("Gardien de la Paix", "حارس أمن")
        result = result.replace("Bourse Doctorale Al-Khawarizmi", "منحة الدكتوراه والذكاء الاصطناعي الخوارزمي")
        result = result.replace("Programme PFE & Insertion", "برنامج تدريب نهاية الدراسة والإدماج المهني")
        result = result.replace("Stage PFE", "تدريب نهاية الدراسة")
        result = result.replace("Stage Pré-embauche", "تدريب ما قبل التوظيف")
        result = result.replace("en Intelligence Artificielle", "في الذكاء الاصطناعي")
        result = result.replace("en Systèmes Embarqués", "في الأنظمة المدمجة")
        return result
    }

    fun localizedTimeAgo(time: String): String {
        if (!isAr) return time
        val lower = time.lowercase()
        return when {
            lower.contains("2h") || lower.contains("2 h") -> "منذ ساعتين"
            lower.contains("1h") || lower.contains("1 h") -> "منذ ساعة"
            lower.contains("3h") || lower.contains("3 h") -> "منذ 3 ساعات"
            lower.contains("4h") || lower.contains("4 h") -> "منذ 4 ساعات"
            lower.contains("5h") || lower.contains("5 h") -> "منذ 5 ساعات"
            lower.contains("1j") || lower.contains("1 j") || lower.contains("hier") -> "منذ يوم"
            lower.contains("2j") || lower.contains("2 j") -> "منذ يومين"
            lower.contains("3j") || lower.contains("3 j") -> "منذ 3 أيام"
            lower.contains("4j") || lower.contains("4 j") -> "منذ 4 أيام"
            lower.contains("5j") || lower.contains("5 j") -> "منذ 5 أيام"
            lower.contains("1 sem") || lower.contains("semaine") -> "منذ أسبوع"
            else -> time
        }
    }
}
