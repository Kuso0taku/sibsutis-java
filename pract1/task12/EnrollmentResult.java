package task12;

// sealed-иерархия: компилятор знает все варианты результата
public sealed interface EnrollmentResult
    permits Accepted, Rejected, WaitListed, Deferred {
}
