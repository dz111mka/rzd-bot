package ru.chepikov.util;

import org.springframework.stereotype.Component;

public class DescriptionCarrier {

    public static String reservedSeat = """
            Плацкартные вагоны
            Это вагоны с местами для лежания, 52 или 54 полки на вагон. Обычно обозначаются как 3 класс.
            
            \t3Э — плацкартный вагон с кондиционером и биотуалетом.
            \t3Т — вагон кондиционируется, биотуалета может не быть.
            \t3Д — в вагоне есть кондиционер. Наличие биотуалета не гарантировано. Разрешена перевозка животных.
            \t3У — аналогично 3Д, но не гарантировано наличие кондиционера.
            \t3Л — кондиционер и биотуалет не предусмотрен.
            """;

    public static String compartment = """
            Купе
            Вагон разделён на закрытые купе по 4 полки в каждом. Всего в вагоне от 32 до 40 мест. Маркируется как 2 класс. В стоимость билета в купе всегда включено постельное бельё.
            
            \t2Э — кондиционируемый вагон повышенной комфортности с 4-местными купе. В стоимость билета входит питание, пресса, санитарно-гигиенический набор. Можно ехать с домашними животными. В вагоне есть биотуалет.
            \t2Э — в двухэтажных поездах — аналогично 2Э в обычных поездах, но не предоставляется гигиенический набор и пресса.
            \t2Б — аналогично 2Э, но наличие биотуалета не гарантировано.
            \t2К — кондиционер и биотуалет в вагоне, можно провозить домашних животных. Дополнительные услуги (кроме постельного белья) в стоимость билета не входят.
            \t2У — аналогично 2К, но не гарантировано наличие биотуалета в вагоне.
            \t2Л — никаких дополнительных опций, в стоимость проезда входит только бельё. В вагоне может не быть кондиционера и биотуалета. Можно перевозить домашних животных.
            \t2Д — купе без дополнительных услуг, можно отказаться от оплаты постельного белья (в других купейный вагонах нельзя). Наличие кондиционера и биотуалета не гарантировано (зависит от того, в каком вагоне выделены места этого класса). Перевозка животных не предусмотрена.
            """;

    public static String luxury = """
            Люкс (СВ)
            Это вагоны с 2-местными купе. Мягкие полки для лежания, в вагоне от 16 до 20 мест. Бельё всегда входит в стоимость проезда, все вагоны кондиционируются. Маркируется как 1 класс.
            
            \t1Б — бизнес-класс. В стоимость билета входят напитки, питание, пресса, гигиенические принадлежности и т.п. Стоимость указана за целое купе, в котором едет 1 взрослый пассажир.Можно везти с собой мелких домашних животных.
            \t1Э — то же, что и 1Б, но можно купить одно место в купе, а не выкупать его целиком.
            \t1У — дополнительные услуги в стоимость билета не включены (кроме постельного белья), но уровень комфорта соответствует первому классу. Разрешён провоз животных.
            \t1Л — вагон СВ. Дополнительные услуги в стоимость билета не входят, предполагается наличие кондиционера, но биотуалета может не быть. Постельное бельё входит в стоимость билета, провоз животных разрешён.
            """;
}
