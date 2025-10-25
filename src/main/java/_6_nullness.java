import data2.Capital;
import data2.Population;
import data2.Village;

void main() {
  var population = new Population(12);

  var village = new Village(population);
  //var kvillage = new KVillage(population);

  var capital = new Capital(population);
  //var kcapital = new KCapital(population);
}
