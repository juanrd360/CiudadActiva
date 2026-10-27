package com.example.ciudadactiva.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ciudadactiva.R
import com.example.ciudadactiva.data.*
import com.example.ciudadactiva.ui.theme.*

// =========================================================================
// 1. LOGO & BRAND COMPOSITION (`Logo/Icono` & `Marca/Composición`)
// Official CiudadActiva circular emblem
// =========================================================================
@Composable
fun CiudadActivaLogoIcon(
    width: Dp = 96.dp,
    tintColor: Color = CaNeutralDark,
    eyeBgColor: Color = CaWhite,
    alpha: Float = 1f
) {
    Image(
        painter = painterResource(id = R.drawable.logo_ciudadactiva),
        contentDescription = "Logo CiudadActiva",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(width)
            .clip(CircleShape)
            .alpha(alpha)
    )
}

@Composable
fun CiudadActivaBrandHeader(
    isOnDarkTurquoise: Boolean = false,
    logoWidth: Dp = 96.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CiudadActivaLogoIcon(
            width = logoWidth,
            tintColor = if (isOnDarkTurquoise) CaWhite else CaNeutralDark,
            eyeBgColor = if (isOnDarkTurquoise) CaPrimary else CaWhite
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = if (isOnDarkTurquoise) CaWhite else CaNeutralDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                ) {
                    append("Ciudad")
                }
                withStyle(
                    SpanStyle(
                        color = if (isOnDarkTurquoise) CaWhite else CaPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                ) {
                    append("Activa")
                }
            },
            style = Titulo24
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "TU CIUDAD, EN ACCIÓN",
            style = MarcaEslogan.copy(
                color = if (isOnDarkTurquoise) CaWhite else CaMuted
            )
        )
    }
}

// =========================================================================
// 2. BUTTONS (`Boton` — 5 types × 3 states, 48dp height, Pill shape)
// =========================================================================
enum class CaButtonType {
    PRIMARIO, ACCION, SECUNDARIO, TEXTO, DESTRUCTIVO
}

@Composable
fun CaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    type: CaButtonType = CaButtonType.PRIMARIO,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val bgColor = when {
        !enabled && !isLoading -> when (type) {
            CaButtonType.SECUNDARIO -> CaWhite
            CaButtonType.TEXTO -> Color.Transparent
            else -> CaBorder
        }
        else -> when (type) {
            CaButtonType.PRIMARIO -> CaPrimary
            CaButtonType.ACCION -> CaAccent
            CaButtonType.SECUNDARIO -> CaWhite
            CaButtonType.TEXTO -> Color.Transparent
            CaButtonType.DESTRUCTIVO -> CaSecondary
        }
    }

    val contentColor = when {
        !enabled && !isLoading -> CaMuted
        else -> when (type) {
            CaButtonType.PRIMARIO -> CaNeutralDark
            CaButtonType.ACCION -> CaNeutralDark
            CaButtonType.SECUNDARIO -> CaNeutralDark
            CaButtonType.TEXTO -> CaPrimaryText
            CaButtonType.DESTRUCTIVO -> CaWhite
        }
    }

    val borderStroke = when (type) {
        CaButtonType.SECUNDARIO -> BorderStroke(
            1.dp,
            if (!enabled && !isLoading) CaBorder else CaPrimaryText
        )
        else -> null
    }

    Button(
        onClick = { if (!isLoading && enabled) onClick() },
        enabled = enabled || isLoading,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor,
            disabledContainerColor = bgColor,
            disabledContentColor = contentColor
        ),
        border = borderStroke,
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Procesando...",
                style = Texto14Semibold.copy(color = contentColor)
            )
        } else {
            Text(
                text = text,
                style = Texto14Semibold.copy(color = contentColor)
            )
        }
    }
}

// =========================================================================
// 3. TEXT FIELDS & SELECTORS (`Campo` & `Selector`)
// Persistent top label, 48dp min height, 12dp radius, inline error
// =========================================================================
@Composable
fun CaTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier.fillMaxWidth(),
    isPassword: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    maxLength: Int? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = !errorMessage.isNullOrBlank()

    Column(modifier = modifier) {
        Text(
            text = label,
            style = Texto12Semibold.copy(
                color = if (isError) CaSecondary else CaMuted
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = Texto14Regular.copy(color = CaMuted)
                )
            },
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Mostrar contraseña",
                            tint = CaMuted
                        )
                    }
                }
            } else null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CaWhite,
                unfocusedContainerColor = CaWhite,
                disabledContainerColor = CaBorder,
                errorContainerColor = CaWhite,
                focusedBorderColor = CaPrimaryText,
                unfocusedBorderColor = CaBorder,
                errorBorderColor = CaSecondary,
                disabledBorderColor = CaBorder,
                focusedTextColor = CaNeutralDark,
                unfocusedTextColor = CaNeutralDark,
                disabledTextColor = CaNeutralDark
            ),
            textStyle = Texto14Regular,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        )

        if (isError || helperText != null || maxLength != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = errorMessage ?: helperText ?: "",
                    style = Texto12Regular.copy(
                        color = if (isError) CaSecondary else CaMuted
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (maxLength != null) {
                    Text(
                        text = "${value.length}/$maxLength",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaDropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    placeholder: String = "Selecciona una opción",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = Texto12Semibold.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue.ifEmpty { placeholder },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = CaNeutralDark
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CaWhite,
                    unfocusedContainerColor = CaWhite,
                    focusedBorderColor = CaPrimaryText,
                    unfocusedBorderColor = CaBorder,
                    focusedTextColor = if (selectedValue.isEmpty()) CaMuted else CaNeutralDark,
                    unfocusedTextColor = if (selectedValue.isEmpty()) CaMuted else CaNeutralDark
                ),
                textStyle = Texto14Regular,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CaWhite)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = Texto14Regular) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. STATUS PILLS, PRIORITY CHIPS, INDICATORS & ROLE CHIPS
// =========================================================================
@Composable
fun CaStatusChip(status: ReportStatus) {
    val (bg, fg) = when (status) {
        ReportStatus.REGISTRADO -> StatusRegistradoBg to StatusRegistradoText
        ReportStatus.ADMITIDO -> StatusAdmitidoBg to StatusAdmitidoText
        ReportStatus.ASIGNADO -> StatusAsignadoBg to StatusAsignadoText
        ReportStatus.EN_ATENCION -> StatusEnAtencionBg to StatusEnAtencionText
        ReportStatus.CERRADO -> StatusCerradoBg to StatusCerradoText
        ReportStatus.CONFIRMADO -> StatusConfirmadoBg to StatusConfirmadoText
        ReportStatus.REABIERTO -> StatusReabiertoBg to StatusReabiertoText
        ReportStatus.RECHAZADO -> StatusRechazadoBg to StatusRechazadoText
        ReportStatus.CANCELADO -> StatusCanceladoBg to StatusCanceladoText
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = status.label,
            style = Texto12Semibold.copy(color = fg),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun CaPriorityChip(
    priority: ReportPriority,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val (color, icon) = when (priority) {
        ReportPriority.BAJA -> PriorityBajaBorder to Icons.Default.ArrowDownward
        ReportPriority.MEDIA -> PriorityMediaBorder to Icons.Default.Remove
        ReportPriority.ALTA -> PriorityAltaBorder to Icons.Default.ArrowUpward
        ReportPriority.CRITICA -> PriorityCriticaBorder to Icons.Outlined.WarningAmber
    }
    Surface(
        color = if (selected) color.copy(alpha = 0.12f) else CaWhite,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = priority.label,
                style = Texto12Semibold.copy(color = color)
            )
        }
    }
}

@Composable
fun CaRoleChip(roleLabel: String) {
    Surface(
        color = StatusAdmitidoBg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = roleLabel,
            style = Texto12Semibold.copy(color = StatusAdmitidoText),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CaVencidoBadge() {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, CaAccent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = StatusEnAtencionText,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Vencido",
                style = Texto12Semibold.copy(color = StatusEnAtencionText)
            )
        }
    }
}

@Composable
fun CaActualizadoIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(CaAccent)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Actualizado",
            style = Texto12Semibold.copy(color = CaMuted)
        )
    }
}

// =========================================================================
// 5. FILTER CHIPS & RATING STARS (`Filtro` & `Calificacion`)
// =========================================================================
@Composable
fun CaFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) StatusAdmitidoBg else CaWhite,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) CaPrimaryText else CaBorder),
        modifier = modifier
            .heightIn(min = 42.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CaPrimaryText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = Texto12Semibold.copy(
                    color = if (selected) CaNeutralDark else CaNeutralDark
                )
            )
        }
    }
}

@Composable
fun CaStarRating(
    rating: Int,
    onRatingChange: ((Int) -> Unit)? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..5) {
            val isSelected = i <= rating
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Estrella $i",
                tint = if (isSelected) CaAccent else CaMuted,
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (onRatingChange != null) {
                            Modifier.clickable { onRatingChange(i) }
                        } else Modifier
                    )
            )
        }
    }
}

// =========================================================================
// 6. REPORT SUMMARY CARD (`Tarjeta reporte`)
// 72x72 thumbnail, code, category, title, status pill, indicator
// =========================================================================
@Composable
fun CaReportThumbnail(
    size: Dp = 72.dp,
    isRealPhotoStyle: Boolean = false,
    onRemove: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isRealPhotoStyle) Color(0xFF4A575C) else ThumbnailBlueBg),
        contentAlignment = Alignment.Center
    ) {
        if (isRealPhotoStyle) {
            // Urban street pothole / infrastructure vector illustration
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = Color(0xFF6B787E))
                // Road lines
                drawLine(
                    color = Color(0xFFE3E6E2),
                    start = Offset(0f, this.size.height * 0.35f),
                    end = Offset(this.size.width, this.size.height * 0.2f),
                    strokeWidth = 3f
                )
                // Pothole dark patch
                drawOval(
                    color = Color(0xFF202B30),
                    topLeft = Offset(this.size.width * 0.2f, this.size.height * 0.45f),
                    size = Size(this.size.width * 0.6f, this.size.height * 0.35f)
                )
                // Orange cone dot
                drawCircle(
                    color = Color(0xFFFF9810),
                    radius = 6f,
                    center = Offset(this.size.width * 0.5f, this.size.height * 0.38f)
                )
            }
        } else {
            // Exact lightbulb / streetlamp icon from Sistema/03
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = CaNeutralDark,
                    modifier = Modifier.size(size * 0.42f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(size * 0.55f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CaPrimaryText)
                )
            }
        }

        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CaWhite.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar foto",
                    tint = CaNeutralDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CaReportCard(
    report: ReportItem,
    onClick: () -> Unit,
    showAuthorInHeader: Boolean = true,
    showPriorityRow: Boolean = false
) {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CaBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                CaReportThumbnail(size = 72.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showAuthorInHeader) "${report.code} · ${report.authorName}" else report.code,
                        style = Texto12Semibold.copy(color = CaPrimaryText)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = report.category,
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.title,
                        style = Titulo16,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaStatusChip(report.status)
                when {
                    report.isOverdue -> CaVencidoBadge()
                    report.hasUnreadUpdate -> CaActualizadoIndicator()
                }
            }
            if (showPriorityRow) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CaPriorityChip(report.priority)
                    if (report.dueInText != null) {
                        Text(
                            text = report.dueInText!!,
                            style = Texto12Semibold.copy(
                                color = if (report.isOverdue) CaSecondary else StatusEnAtencionText
                            )
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 7. TOP APP BAR, TABS & ROLE-BASED BOTTOM NAVIGATION BAR
// =========================================================================
@Composable
fun CaTopAppBar(
    title: String,
    showLogo: Boolean = true,
    onBackClick: (() -> Unit)? = null,
    notificationCount: Int? = null,
    onNotificationClick: (() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Surface(
        color = CaNeutralLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = CaNeutralDark
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else if (showLogo) {
                CiudadActivaLogoIcon(width = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = title,
                style = Titulo20,
                modifier = Modifier.weight(1f)
            )

            if (trailingContent != null) {
                trailingContent()
            } else if (onNotificationClick != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Avisos",
                        tint = CaNeutralDark,
                        modifier = Modifier.size(24.dp)
                    )
                    if (notificationCount != null && notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(CaAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = notificationCount.toString(),
                                style = Texto12Semibold.copy(
                                    fontSize = 10.sp,
                                    color = CaNeutralDark
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = title,
                        style = if (selected) {
                            Texto14Semibold.copy(color = CaPrimaryText)
                        } else {
                            Texto14Regular.copy(color = CaMuted)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(3.dp)
                            .background(if (selected) CaPrimaryText else Color.Transparent)
                    )
                }
            }
        }
        HorizontalDivider(color = CaBorder, thickness = 1.dp)
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun CaBottomNavigationBar(
    role: UserRole,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = when (role) {
        UserRole.CIUDADANO -> listOf(
            BottomNavItem("citizen_reports", "Reportes", Icons.Outlined.Map),
            BottomNavItem("create_report", "Crear", Icons.Outlined.AddCircleOutline),
            BottomNavItem("my_reports", "Mis reportes", Icons.Outlined.Assignment),
            BottomNavItem("notifications", "Avisos", Icons.Outlined.Notifications),
            BottomNavItem("profile", "Perfil", Icons.Outlined.PersonOutline)
        )
        UserRole.GESTOR -> listOf(
            BottomNavItem("gestor_inbox", "Bandeja", Icons.Outlined.Inbox),
            BottomNavItem("citizen_reports", "Reportes", Icons.Outlined.Map),
            BottomNavItem("indicators", "Indicadores", Icons.Outlined.BarChart),
            BottomNavItem("notifications", "Avisos", Icons.Outlined.Notifications),
            BottomNavItem("profile", "Perfil", Icons.Outlined.PersonOutline)
        )
        UserRole.OPERADOR -> listOf(
            BottomNavItem("operador_assigned", "Asignados", Icons.Outlined.Assignment),
            BottomNavItem("citizen_reports", "Reportes", Icons.Outlined.Map),
            BottomNavItem("notifications", "Avisos", Icons.Outlined.Notifications),
            BottomNavItem("profile", "Perfil", Icons.Outlined.PersonOutline)
        )
        UserRole.ADMINISTRADOR -> listOf(
            BottomNavItem("admin_reports", "Reportes", Icons.Outlined.Map),
            BottomNavItem("admin_categories", "Categorías", Icons.Outlined.Layers),
            BottomNavItem("admin_users", "Usuarios", Icons.Outlined.Group),
            BottomNavItem("indicators", "Indicadores", Icons.Outlined.BarChart),
            BottomNavItem("profile", "Perfil", Icons.Outlined.PersonOutline)
        )
    }

    Surface(
        color = CaWhite,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = CaBorder, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate(item.route) }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) StatusAdmitidoBg else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) CaPrimaryText else CaNeutralDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            style = Texto12Regular.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) CaPrimaryText else CaMuted
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 8. MAP PLACEHOLDER & MARKER (`Mapa` & `Marcador mapa`)
// =========================================================================
@Composable
fun CaCityMapPlaceholder(
    locationLabel: String = "Ubicación del reporte",
    height: Dp = 180.dp,
    multiplePins: Boolean = false,
    showGpsBadge: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(CaWhite)
            .border(1.dp, CaBorder, RoundedCornerShape(16.dp))
    ) {
        // City blocks grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blockColor = Color(0xFFE3E6E2)
            val parkColor = Color(0xFFCCEFED)
            val w = size.width
            val h = size.height

            // Row 1 blocks
            drawRoundRect(blockColor, Offset(0f, 0f), Size(w * 0.36f, h * 0.42f), CornerRadius(8f))
            drawRoundRect(blockColor, Offset(w * 0.40f, 0f), Size(w * 0.59f, h * 0.42f), CornerRadius(8f))

            // Row 2 blocks
            drawRoundRect(blockColor, Offset(0f, h * 0.48f), Size(w * 0.36f, h * 0.52f), CornerRadius(8f))
            drawRoundRect(blockColor, Offset(w * 0.40f, h * 0.48f), Size(w * 0.59f, h * 0.52f), CornerRadius(8f))

            // Turquoise park area
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(w * 0.62f, h * 0.18f),
                size = Size(w * 0.28f, h * 0.32f),
                cornerRadius = CornerRadius(24f, 24f)
            )
        }

        // Main Orange Marker
        CaMapPin(
            modifier = Modifier
                .align(if (multiplePins) Alignment.Center else Alignment.TopStart)
                .padding(start = 14.dp, top = 10.dp)
        )

        if (multiplePins) {
            CaMapPin(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 52.dp, top = 42.dp),
                scale = 0.8f
            )
            CaMapPin(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, bottom = 36.dp),
                scale = 0.8f
            )
            CaMapPin(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 56.dp, bottom = 44.dp),
                scale = 0.8f
            )
        }

        if (showGpsBadge) {
            Surface(
                color = StatusAdmitidoBg,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = "GPS",
                    style = Texto12Semibold.copy(color = StatusAdmitidoText),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
        }

        if (locationLabel.isNotEmpty()) {
            Surface(
                color = CaWhite,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = CaPrimaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = locationLabel,
                        style = Texto12Regular.copy(color = CaNeutralDark)
                    )
                }
            }
        }
    }
}

@Composable
fun CaMapPin(modifier: Modifier = Modifier, scale: Float = 1f) {
    Canvas(
        modifier = modifier
            .width((36 * scale).dp)
            .height((44 * scale).dp)
    ) {
        val w = size.width
        val h = size.height
        val pinPath = Path().apply {
            moveTo(w * 0.5f, h * 0.95f)
            cubicTo(w * 0.15f, h * 0.62f, w * 0.05f, h * 0.45f, w * 0.05f, h * 0.36f)
            cubicTo(w * 0.05f, h * 0.12f, w * 0.25f, h * 0.04f, w * 0.5f, h * 0.04f)
            cubicTo(w * 0.75f, h * 0.04f, w * 0.95f, h * 0.12f, w * 0.95f, h * 0.36f)
            cubicTo(w * 0.95f, h * 0.45f, w * 0.85f, h * 0.62f, w * 0.5f, h * 0.95f)
            close()
        }
        drawPath(pinPath, color = CaAccent)
        drawPath(pinPath, color = CaNeutralDark, style = Stroke(width = 3f))
        drawCircle(
            color = CaWhite,
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.36f)
        )
        drawCircle(
            color = CaNeutralDark,
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.36f),
            style = Stroke(width = 2.5f)
        )
    }
}

// =========================================================================
// 9. EMPTY, ERROR, SKELETON & SNACKBAR STATES (`Vacio`, `Error`, `Skeleton`, `Snackbar`)
// =========================================================================
@Composable
fun CaEmptyStateCard(
    title: String,
    subtitle: String,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null
) {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            CiudadActivaLogoIcon(
                width = 80.dp,
                tintColor = CaMuted,
                alpha = 0.6f
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = Titulo20,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = Texto14Regular.copy(color = CaMuted),
                textAlign = TextAlign.Center
            )
            if (ctaText != null && onCtaClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                CaButton(
                    text = ctaText,
                    onClick = onCtaClick,
                    type = CaButtonType.PRIMARIO
                )
            }
        }
    }
}

@Composable
fun CaErrorStateCard(
    title: String = "No pudimos cargar",
    subtitle: String = "Revisa tu conexión a internet e intenta de nuevo.",
    onRetry: () -> Unit
) {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = CaSecondary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = Titulo20,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = Texto14Regular.copy(color = CaMuted),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            CaButton(
                text = "Reintentar",
                onClick = onRetry,
                type = CaButtonType.SECUNDARIO
            )
        }
    }
}

@Composable
fun CaSkeletonCard() {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CaBorder)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CaBorder)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CaBorder)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CaBorder)
                )
            }
        }
    }
}

@Composable
fun CaSnackbarBanner(
    message: String,
    isError: Boolean = false,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        color = if (isError) CaSecondary else CaPrimary,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (isError) CaWhite else CaNeutralDark,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = Texto14Regular.copy(
                    color = if (isError) CaWhite else CaNeutralDark
                ),
                modifier = Modifier.weight(1f)
            )
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionText,
                    style = Texto14Semibold.copy(
                        color = if (isError) CaWhite else CaNeutralDark
                    ),
                    modifier = Modifier.clickable { onActionClick() }
                )
            }
        }
    }
}

@Composable
fun CaConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String = "Cancelar",
    confirmType: CaButtonType = CaButtonType.DESTRUCTIVO,
    showLogoutIcon: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CaWhite,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                if (showLogoutIcon) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = CaSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(text = title, style = Titulo20)
            }
        },
        text = {
            Text(
                text = message,
                style = Texto14Regular.copy(color = CaMuted)
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = cancelText,
                        style = Texto14Semibold.copy(color = CaPrimaryText)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                CaButton(
                    text = confirmText,
                    onClick = onConfirm,
                    type = confirmType,
                    modifier = Modifier.widthIn(min = 140.dp)
                )
            }
        }
    )
}
