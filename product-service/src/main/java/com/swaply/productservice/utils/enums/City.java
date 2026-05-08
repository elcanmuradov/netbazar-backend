package com.swaply.productservice.utils.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum City {
    BAKI("Bakı"),
    GENCE("Gəncə"),
    SUMQAYIT("Sumqayıt"),
    MINGECEVIR("Mingəçevir"),
    SEKI("Şəki"),
    YEVLAX("Yevlax"),
    LENKERAN("Lənkəran"),
    SIRVAN("Şirvan"),
    NAFTALAN("Naftalan"),
    SUSA("Şuşa"),
    XANKENDI("Xankəndi"),

    // Abşeron rayonu
    ABSHERON("Abşeron"),

    // Bölgə rayonları (A-Z)
    AGCABEDI("Ağcabədi"),
    AGDAM("Ağdam"),
    AGDAS("Ağdaş"),
    AGSTAF("Ağstafa"),
    AGSU("Ağsu"),
    ASTARA("Astara"),
    BALAKEN("Balakən"),
    BERDE("Bərdə"),
    BEYLEQAN("Beyləqan"),
    BILESUVAR("Biləsuvar"),
    CABRAYIL("Cəbrayıl"),
    CELILABAD("Cəlilabad"),
    DASKESAN("Daşkəsən"),
    FUZULI("Füzuli"),
    GEDEBEY("Gədəbəy"),
    GORANBOY("Goranboy"),
    GOYCHAY("Göyçay"),
    GOYGOL("Göygöl"),
    HACIQABUL("Hacıqabul"),
    IMISHLI("İmişli"),
    ISMAILLI("İsmayıllı"),
    KALBAJAR("Kəlbəcər"),
    KURDAMIR("Kürdəmir"),
    LACHIN("Laçın"),
    LERIK("Lerik"),
    MASALLI("Masallı"),
    NEFTCHALA("Neftçala"),
    OGUZ("Oğuz"),
    GABALA("Qəbələ"),
    GAKH("Qax"),
    GAZAKH("Qazax"),
    GOBUSTAN("Qobustan"),
    GUBA("Quba"),
    GUBADLI("Qubadlı"),
    GUSAR("Qusar"),
    SAATLI("Saatlı"),
    SABIRABAD("Sabirabad"),
    SALYAN("Salyan"),
    SHAMAKHI("Şamaxı"),
    SHAMKIR("Şəmkir"),
    SIYAZAN("Siyəzən"),
    TARTAR("Tərtər"),
    TOVUZ("Tovuz"),
    UCAR("Ucar"),
    KHACHMAZ("Xaçmaz"),
    KHIZI("Xızı"),
    KHOJALI("Xocalı"),
    KHOJAVEND("Xocavənd"),
    YARDIMLI("Yardımlı"),
    ZANGILAN("Zəngilan"),
    ZAGATALA("Zaqatala"),
    ZARDAB("Zərdab"),

    // Naxçıvan Muxtar Respublikası şəhər və rayonları
    NAKHCHIVAN_CITY("Naxçıvan (Şəhər)"),
    BABEK("Babək"),
    CULFA("Culfa"),
    KANGARLI("Kəngərli"),
    ORDUBAD("Ordubad"),
    SADEREK("Sədərək"),
    SHAHBUZ("Şahbuz"),
    SHARUR("Şərur");

    private final String displayName;

    /**
     * Enum adını (məs: "BAKI") orijinal ada (məs: "Bakı") çevirir.
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}