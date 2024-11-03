package data2

class KVillage {
    private val population : Population;

    constructor(population : Population) {
        println(this);
        this.population = population;
    }

    override fun toString(): String {
        return "data2.KVillage(" + population + ")";
    }
}
