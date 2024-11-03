package data2

class KCapital : KCity {
    private val population: Population

    constructor(population: Population) : super() {
        this.population = population;
    }

    override fun init() {
        println(this);
    }

    override fun toString(): String {
        return "data.KCapital(${population})";
    }

    fun population(): Population {
        return population
    }
}
