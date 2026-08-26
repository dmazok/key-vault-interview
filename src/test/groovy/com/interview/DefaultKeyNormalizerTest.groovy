package com.interview

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class DefaultKeyNormalizerTest extends Specification {

    @Subject
    KeyNormalizer keyNormalizer = new DefaultKeyNormalizer()

    def 'Should normalize string keys to lower case'() {
        expect:
        keyNormalizer.normalize(key) == new TypedKey(KeyType.STRING, expected)

        where:
        key                     || expected
        'key'                   || 'key'
        'KEY'                   || 'key'
        'SoMe_Key'              || 'some_key'
        'a'                     || 'a'
        'qwertyuiopqwertyuiop'  || 'qwertyuiopqwertyuiop'   // 20 char limit
    }

    def 'Should keep surrounding whitespace of a string key'() {
        expect:
        keyNormalizer.normalize(' key ') == new TypedKey(KeyType.STRING, ' key ')
    }

    def 'Should reject blank string keys'() {
        when:
        keyNormalizer.normalize(key)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'

        where:
        key << ['', ' ', '   ', '\t', '\n']
    }

    def 'Should reject string keys longer than 20 characters'() {
        when:
        keyNormalizer.normalize('qwertyuiopqwertyuiop1')

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'
    }

    def 'Should reject a null string key'() {
        when:
        keyNormalizer.normalize((String) null)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'
    }

    def 'Should normalize positive integral numbers to their canonical form'() {
        expect:
        keyNormalizer.normalize(key) == new TypedKey(KeyType.NUMBER, expected)

        where:
        key                         || expected
        7 as Integer                || '7'
        7L                          || '7'
        (short) 7                   || '7'
        (byte) 7                    || '7'
        7.0f                        || '7'
        7.0d                        || '7'
        new BigDecimal('7.00')      || '7'
        new BigInteger('7')         || '7'
        new AtomicInteger(7)        || '7'
        new AtomicLong(7L)          || '7'
        42                          || '42'
        new BigDecimal('1E+3')      || '1000'
        1e21d                       || '1000000000000000000000'
        Long.MAX_VALUE              || '9223372036854775807'
    }

    def 'Should reject non-positive number keys'() {
        when:
        keyNormalizer.normalize(key)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'

        where:
        key << [0, 0L, 0.0d, -0.0d, new BigDecimal('0.00'),
                -1, -1L, -7.0d, new BigInteger('-3')]
    }

    def 'Should reject fractional number keys'() {
        when:
        keyNormalizer.normalize(key)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'

        where:
        key << [7.5d, 0.1d, 1.5f, new BigDecimal('7.01'), 0.1d + 0.2d, -2.5d]
    }

    def 'Should reject non-finite number keys'() {
        when:
        keyNormalizer.normalize(key)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'

        where:
        key << [Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY]
    }

    def 'Should reject a null number key'() {
        when:
        keyNormalizer.normalize((Number) null)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid argument'
    }

    def 'Should not apply the string length limit to number keys'() {
        given:
        def key = new BigInteger('1' * 30)

        expect:
        keyNormalizer.normalize(key) == new TypedKey(KeyType.NUMBER, '1' * 30)
    }

    def 'Should place equal text under different key types'() {
        given:
        def stringKey = keyNormalizer.normalize('7')
        def numberKey = keyNormalizer.normalize(7)

        expect:
        stringKey.keyValue() == numberKey.keyValue()

        and:
        stringKey.keyType() == KeyType.STRING
        numberKey.keyType() == KeyType.NUMBER

        and:
        stringKey != numberKey
    }
}
