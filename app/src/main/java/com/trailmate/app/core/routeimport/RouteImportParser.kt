package com.trailmate.app.core.routeimport

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import javax.xml.XMLConstants
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

private const val AccessExternalDtd = "http://javax.xml.XMLConstants/property/accessExternalDTD"
private const val AccessExternalSchema = "http://javax.xml.XMLConstants/property/accessExternalSchema"
private val DisallowedXmlDeclaration = Regex(
    pattern = """<!\s*(DOCTYPE|ENTITY)\b""",
    option = RegexOption.IGNORE_CASE,
)

object RouteImportParser {
    fun parse(
        fileName: String,
        content: String,
        options: RouteImportOptions = RouteImportOptions(),
    ): RouteImportResult {
        val format = detectFormat(fileName) ?: return RouteImportResult.rejected(
            fileName = fileName,
            format = null,
            status = RouteImportStatus.UnsupportedFormat,
            warning = RouteImportWarning.UnsupportedFormat,
        )
        val document = parseXml(content) ?: return RouteImportResult.rejected(
            fileName = fileName,
            format = format,
            status = RouteImportStatus.InvalidXml,
            warning = RouteImportWarning.InvalidXml,
        )

        return when (format) {
            RouteImportFormat.Gpx -> GpxRouteImportParser.parse(fileName, document, options)
            RouteImportFormat.Kml -> KmlRouteImportParser.parse(fileName, document, options)
        }
    }

    private fun detectFormat(fileName: String): RouteImportFormat? =
        when (fileName.substringAfterLast('.', "").lowercase()) {
            "gpx" -> RouteImportFormat.Gpx
            "kml" -> RouteImportFormat.Kml
            else -> null
        }

    private fun parseXml(content: String): Document? {
        val normalizedContent = content.removePrefix("\uFEFF")

        if (DisallowedXmlDeclaration.containsMatchIn(normalizedContent)) {
            return null
        }

        return runCatching {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            runCatching {
                factory.isXIncludeAware = false
            }
            runCatching {
                factory.isExpandEntityReferences = false
            }
            factory.disableExternalXml()
            factory.newDocumentBuilder()
                .apply {
                    setEntityResolver(EntityResolver { _, _ ->
                        InputSource(StringReader(""))
                    })
                }
                .parse(InputSource(StringReader(normalizedContent)))
        }.getOrNull()
    }
}

private fun DocumentBuilderFactory.disableExternalXml() {
    val disabledFeatures = mapOf(
        "http://apache.org/xml/features/disallow-doctype-decl" to true,
        "http://xml.org/sax/features/external-general-entities" to false,
        "http://xml.org/sax/features/external-parameter-entities" to false,
        "http://apache.org/xml/features/nonvalidating/load-external-dtd" to false,
    )
    disabledFeatures.forEach { (feature, enabled) ->
        runCatching {
            setFeature(feature, enabled)
        }
    }
    runCatching {
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }
    runCatching {
        setAttribute(AccessExternalDtd, "")
    }
    runCatching {
        setAttribute(AccessExternalSchema, "")
    }
}

internal fun Document.elementsByLocalName(localName: String): List<Element> {
    val namespaced = getElementsByTagNameNS("*", localName).toElementList()
    return namespaced.ifEmpty { getElementsByTagName(localName).toElementList() }
}

internal fun Element.directChild(localName: String): Element? =
    childNodes
        .toElementList()
        .firstOrNull { it.localTagName() == localName }

internal fun Element.directChildren(localName: String): List<Element> =
    childNodes
        .toElementList()
        .filter { it.localTagName() == localName }

internal fun Element.directText(localName: String): String? =
    directChild(localName)?.textContent?.trim()?.takeIf { it.isNotBlank() }

internal fun Element.localTagName(): String =
    localName ?: tagName.substringAfter(':')

internal fun NodeList.toElementList(): List<Element> =
    (0 until length)
        .map { item(it) }
        .filter { it.nodeType == Node.ELEMENT_NODE }
        .map { it as Element }
