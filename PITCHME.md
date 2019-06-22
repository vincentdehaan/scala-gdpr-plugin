## Befriend the compiler and enhance GDPR compliance
### Vincent de Haan

---

## Who am I?

---

## Goals for today

- Look inside the Scala compiler
- Develop a nice compiler plugin to enhance GDPR compliance

---

@snap[north span-100]
## Art. 30, sect. 1 of the GDPR
@snapend

@snap[west span-50]
> Each controller [...] shall maintain a record of processing activities under its responsibility. That record shall contain all of the following information:
> [...]
> (b) the purposes of the processing;
> (c) [...] the categories of personal data;
> [...]
@snapend

@snap[east span-50]
[ul]
- What is a _processing_?
- How can the compliance officer have _complete_ record of all processings?
[ulend]
@snapend

---

@snap[north span-100]
## The idea
Use @annotations
@snapend

---

@snap[east span-50]
```
object CustomerRepository {
    
    def getCustomerById(id: String) = ???


    def getOrderById(id: String) = ???
}
```

@snap[west span-50]
```
object SomeFeature {
    val customer = getCustomerById(id)

    val order = getOrderById(orderId)    
}
```

---

@snap[east span-50]
```
object CustomerRepository {
    @Processing
    def getCustomerById(id: String) = ???

    @Processing
    def getOrderById(id: String) = ???
}
```
@snapend

@snap[west span-50]
```
object SomeFeature {
    val customer = getCustomerById(id) 
        : @ProcessingInstance(purpose = "Customer support")

    val order = getOrderById(orderId) 
        : @ProcessingInstance(purpose = "Some other purpose")
}
```
@snapend

---

## How does the compiler work?
Abstract syntax tree transformation

---

@snap[west snap-50]
```
val x = 1 + 2 * 3
```
@snapend
@snap[east snap-50]
```
ValDef(
  0,
  "x",
  <tpt>,
  Apply(
    1."$plus",
    Apply(
      2."$times",
      3
    )
  )
)
```