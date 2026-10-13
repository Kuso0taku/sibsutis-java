package task14;

// комбинации собираются из компонентов: 3x3 = 9 вариантов без девяти подклассов
public class Task14Demo {
  public static void main(String[] args) {
    CourseOffer[] offers = {
        new CourseOffer(new Online(), new Free()),
        new CourseOffer(new Online(), new Fixed(5000)),
        new CourseOffer(new Online(), new Subscription()),
        new CourseOffer(new Classroom(), new Free()),
        new CourseOffer(new Classroom(), new Fixed(5000)),
        new CourseOffer(new Classroom(), new Subscription()),
        new CourseOffer(new Hybrid(), new Free()),
        new CourseOffer(new Hybrid(), new Fixed(5000)),
        new CourseOffer(new Hybrid(), new Subscription())
    };

    for (CourseOffer offer : offers) {
      System.out.println(offer.describe(12000));
    }

    // добавление новой оси: без правок CourseOffer и без новых комбинированных классов
    System.out.println(new CourseOffer(new SelfPaced(), new Installment(4)).describe(12000));
  }
}
