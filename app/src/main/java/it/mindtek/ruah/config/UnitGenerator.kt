package it.mindtek.ruah.config

import it.mindtek.ruah.R
import it.mindtek.ruah.db.models.ModelUnit

/**
 * Created by alessandrogaboardi on 29/11/2017.
 */
class UnitGenerator {
    companion object {
        fun getUnits(): MutableList<ModelUnit>{
            val units: MutableList<ModelUnit> = mutableListOf()
            val unit1 = ModelUnit(
                    0,
                    R.drawable.accoglienza,
                    R.string.accoglienza,
                    R.color.accoglienza,
                    1,
                    enabled = true
            )
            units.add(unit1)
            val unit2 = ModelUnit(
                    1,
                    R.drawable.lavoro,
                    R.string.lavoro,
                    R.color.lavoro,
                    2
            )
            units.add(unit2)
            val unit3 = ModelUnit(
                    2,
                    R.drawable.cibo,
                    R.string.cibo,
                    R.color.cibo,
                    3
            )
            units.add(unit3)
            val unit4 = ModelUnit(
                    3,
                    R.drawable.telefono,
                    R.string.telefono,
                    R.color.telefono,
                    4
            )
            units.add(unit4)
            val unit5 = ModelUnit(
                    4,
                    R.drawable.stato,
                    R.string.stato,
                    R.color.stato,
                    5
            )
            units.add(unit5)
            val unit6 = ModelUnit(
                    5,
                    R.drawable.salute,
                    R.string.salute,
                    R.color.salute,
                    6
            )
            units.add(unit6)
            val unit7 = ModelUnit(
                    6,
                    R.drawable.citta,
                    R.string.citta,
                    R.color.citta,
                    7
            )
            units.add(unit7)
            val unit8 = ModelUnit(
                    7,
                    R.drawable.mezzi,
                    R.string.mezzi,
                    R.color.mezzi,
                    8
            )
            units.add(unit8)
            val unit9= ModelUnit(
                    8,
                    R.drawable.casa,
                    R.string.casa,
                    R.color.casa,
                    9
            )
            units.add(unit9)
            val unit10 = ModelUnit(
                    9,
                    R.drawable.viaggio,
                    R.string.viaggio,
                    R.color.viaggio,
                    10
            )
            units.add(unit10)
            return units
        }
    }
}